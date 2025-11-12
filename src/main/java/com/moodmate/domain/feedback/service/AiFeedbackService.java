package com.moodmate.domain.feedback.service;

import com.moodmate.domain.diary.entity.Diary;
import com.moodmate.domain.diary.repository.DiaryRepository;
import com.moodmate.domain.feedback.dto.*;
import com.moodmate.domain.feedback.entity.AiFeedback;
import com.moodmate.domain.feedback.entity.DailyFeedbackUsage;
import com.moodmate.domain.feedback.entity.FeedbackProcessingLock;
import com.moodmate.domain.feedback.repository.AiFeedbackRepository;
import com.moodmate.domain.feedback.repository.DailyFeedbackUsageRepository;
import com.moodmate.domain.feedback.repository.FeedbackProcessingLockRepository;
import com.moodmate.domain.user.entity.User;
import com.moodmate.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiFeedbackService {

    private final AiFeedbackRepository aiFeedbackRepository;
    private final DailyFeedbackUsageRepository dailyUsageRepository;
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;
    private final FeedbackProcessingLockRepository lockRepository;

    private static final int DAILY_FEEDBACK_LIMIT = 2;
    private static final int LOCK_TIMEOUT_MINUTES = 5;

    /**
     * 피드백 생성 (동시성 제어 및 덮어쓰기 지원)
     */
    @Transactional
    public FeedbackResponse createFeedback(Long userId, Long diaryId, FeedbackStyleRequest request) throws AccessDeniedException {
        String lockKey = UUID.randomUUID().toString();

        try {
            // 1. 락 획득 시도
            acquireLock(userId, diaryId, lockKey);

            // 2. 사용자 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            // 3. 일기 조회 및 권한 확인
            Diary diary = diaryRepository.findById(diaryId)
                    .orElseThrow(() -> new IllegalArgumentException("일기를 찾을 수 없습니다."));

            if (!Objects.equals(userId, diary.getUser().getId())) {
                throw new AccessDeniedException("해당 일기에 대한 권한이 없습니다.");
            }

            // 4. 기존 피드백 확인 및 삭제 (덮어쓰기)
            Optional<AiFeedback> existingFeedback = aiFeedbackRepository.findLatestByDiaryId(diaryId);
            boolean isOverwrite = existingFeedback.isPresent();

            if (isOverwrite) {
                // 덮어쓰기 모드: 기존 피드백 삭제 (사용량은 감소시키지 않음)
                AiFeedback oldFeedback = existingFeedback.get();
                log.info("기존 피드백 삭제 (덮어쓰기) - 일기 ID: {}, 피드백 ID: {}", diaryId, oldFeedback.getId());
                aiFeedbackRepository.delete(oldFeedback);
            }

            // 5. 일일 사용량 확인 및 업데이트 (덮어쓰기든 신규든 항상 사용량 증가)
            checkAndUpdateDailyUsage(userId);

            // 6. AI 분석 실행 (락이 걸린 상태에서 실행)
            String summary = geminiService.generateSummary(diary.getContent());
            String response = geminiService.generateFeedback(diary.getContent(), request.feedbackStyle());

            // 7. 피드백 저장
            AiFeedback feedback = AiFeedback.builder()
                    .user(user)
                    .diary(diary)
                    .summary(summary)
                    .response(response)
                    .feedbackStyle(request.feedbackStyle())
                    .requestedAt(LocalDateTime.now())
                    .build();

            aiFeedbackRepository.save(feedback);
            log.info("피드백 {} 완료 - 사용자: {}, 일기: {}",
                    isOverwrite ? "덮어쓰기" : "생성", userId, diaryId);

            return new FeedbackResponse(feedback);

        } finally {
            // 8. 락 해제 (항상 실행)
            releaseLock(lockKey);
        }
    }

    /**
     * 락 획득 (별도 트랜잭션)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void acquireLock(Long userId, Long diaryId, String lockKey) {
        // 오래된 락 정리 (타임아웃 처리)
        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(LOCK_TIMEOUT_MINUTES);
        int deletedCount = lockRepository.deleteExpiredLocks(timeoutTime);
        if (deletedCount > 0) {
            log.info("만료된 락 {}개 정리됨", deletedCount);
        }

        // 기존 락 확인
        Optional<FeedbackProcessingLock> existingLock = lockRepository.findByUserId(userId);
        if (existingLock.isPresent()) {
            log.warn("피드백 처리 중 - 사용자: {}, 일기: {}", userId, diaryId);
            throw new IllegalStateException("이미 피드백을 생성 중입니다. 잠시 후 다시 시도해주세요.");
        }

        // 새 락 생성
        User user = userRepository.getReferenceById(userId);
        FeedbackProcessingLock lock = FeedbackProcessingLock.builder()
                .user(user)
                .diaryId(diaryId)
                .lockKey(lockKey)
                .build();

        try {
            lockRepository.save(lock);
            log.info("락 획득 성공 - 사용자: {}, 일기: {}, 락키: {}", userId, diaryId, lockKey);
        } catch (Exception e) {
            log.error("락 획득 실패 - 사용자: {}, 일기: {}, 오류: {}", userId, diaryId, e.getMessage());
            throw new IllegalStateException("피드백 생성 요청을 처리할 수 없습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    /**
     * 락 해제 (별도 트랜잭션)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void releaseLock(String lockKey) {
        try {
            lockRepository.deleteByLockKey(lockKey);
            log.info("락 해제 완료 - 락키: {}", lockKey);
        } catch (Exception e) {
            log.error("락 해제 중 오류 발생 - 락키: {}, 오류: {}", lockKey, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public FeedbackResponse getFeedback(Long userId, Long diaryId) throws AccessDeniedException {
        // 일기 조회 및 권한 확인
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("일기를 찾을 수 없습니다."));

        if (!Objects.equals(diary.getUser().getId(), userId)) {
            throw new AccessDeniedException("해당 일기에 대한 권한이 없습니다.");
        }

        // 가장 최근 피드백 조회
        return aiFeedbackRepository.findLatestByDiaryId(diaryId)
                .map(FeedbackResponse::new)
                .orElseGet(() -> new FeedbackResponse(
                        null,
                        "피드백이 아직 생성되지 않았습니다.",
                        "피드백이 아직 생성되지 않았습니다.", null, null, null
                ));
    }

    @Transactional(readOnly = true)
    public FeedbackHistoryResponse getFeedbackHistory(Long userId, LocalDate startDate, LocalDate endDate) {
        List<AiFeedback> feedbacks = aiFeedbackRepository.findByUserIdAndDateRange(userId, startDate, endDate);

        List<FeedbackHistoryItem> items = feedbacks.stream()
                .map(FeedbackHistoryItem::new)
                .toList();

        return new FeedbackHistoryResponse(startDate, endDate, items);
    }

    @Transactional(readOnly = true)
    public PeriodAnalysisResponse generatePeriodAnalysis(Long userId, PeriodAnalysisRequest request) {
        // 요청 검증
        request.validate();

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        log.info("기간별 분석 시작 - 사용자: {}, 기간: {} ~ {}", userId, startDate, endDate);

        // 해당 기간의 피드백들 조회
        List<AiFeedback> feedbacks = aiFeedbackRepository.findByUserIdAndDateRange(userId, startDate, endDate);

        if (feedbacks.isEmpty()) {
            throw new IllegalArgumentException("해당 기간에 분석할 일기 데이터가 없습니다.");
        }

        // 요약들을 결합
        String combinedSummaries = feedbacks.stream()
                .map(AiFeedback::getSummary)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n\n"));

        if (combinedSummaries.trim().isEmpty()) {
            throw new IllegalArgumentException("분석할 요약 데이터가 없습니다.");
        }

        try {
            // AI를 통한 종합 분석
            String periodSummary = geminiService.generatePeriodSummary(combinedSummaries, startDate, endDate);
            String recommendations = geminiService.generateRecommendations(combinedSummaries);

            log.info("기간별 분석 완료 - 사용자: {}, 분석된 일기 수: {}", userId, feedbacks.size());

            return PeriodAnalysisResponse.create(
                    startDate,
                    endDate,
                    feedbacks.size(),
                    periodSummary,
                    recommendations
            );

        } catch (Exception e) {
            log.error("기간별 분석 중 오류 발생 - 사용자: {}, 오류: {}", userId, e.getMessage(), e);
            throw new RuntimeException("기간별 분석을 생성하는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public DailyUsageResponse getDailyUsage(Long userId) {
        LocalDate today = LocalDate.now();
        DailyFeedbackUsage usage = dailyUsageRepository.findByUserIdAndUsageDate(userId, today)
                .orElse(null);

        int usedCount = (usage != null) ? usage.getUsageCount() : 0;
        int remainingCount = Math.max(0, DAILY_FEEDBACK_LIMIT - usedCount);

        return new DailyUsageResponse(usedCount, DAILY_FEEDBACK_LIMIT, remainingCount);
    }

    /**
     * 피드백 삭제 (사용량은 감소시키지 않음)
     */
    @Transactional
    public void deleteFeedback(Long userId, Long diaryId) throws AccessDeniedException {
        // 일기 조회 및 권한 확인
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("일기를 찾을 수 없습니다."));

        if (!Objects.equals(diary.getUser().getId(), userId)) {
            throw new AccessDeniedException("해당 일기에 대한 권한이 없습니다.");
        }

        // 가장 최근 피드백 조회
        AiFeedback feedback = aiFeedbackRepository.findLatestByDiaryId(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 일기에 대한 피드백이 없습니다."));

        // 피드백 소유자 확인 (추가 보안)
        if (!Objects.equals(feedback.getUser().getId(), userId)) {
            throw new AccessDeniedException("해당 피드백에 대한 권한이 없습니다.");
        }

        // 피드백 삭제 (사용량은 감소시키지 않음)
        aiFeedbackRepository.delete(feedback);
        log.info("피드백 삭제 완료 - 사용자: {}, 일기: {}, 피드백: {}", userId, diaryId, feedback.getId());
    }

    /**
     * 일일 사용량 확인 및 증가
     */
    private void checkAndUpdateDailyUsage(Long userId) {
        LocalDate today = LocalDate.now();

        DailyFeedbackUsage usage = dailyUsageRepository.findByUserIdAndUsageDate(userId, today)
                .orElse(null);

        if (usage == null) {
            // 오늘 첫 사용
            User user = userRepository.getReferenceById(userId);
            usage = DailyFeedbackUsage.builder()
                    .user(user)
                    .usageDate(today)
                    .usageCount(1)
                    .build();
            dailyUsageRepository.save(usage);
            log.info("새로운 일일 사용량 기록 생성 - 사용자: {}", userId);
        } else {
            // 사용량 확인
            if (usage.getUsageCount() >= DAILY_FEEDBACK_LIMIT) {
                log.warn("일일 사용량 초과 - 사용자: {}, 현재 사용량: {}", userId, usage.getUsageCount());
                throw new IllegalStateException("일일 피드백 사용량을 초과했습니다. (최대 " + DAILY_FEEDBACK_LIMIT + "회)");
            }
            // 사용량 증가
            usage.incrementUsage();
            dailyUsageRepository.save(usage);
            log.info("일일 사용량 업데이트 - 사용자: {}, 현재 사용량: {}", userId, usage.getUsageCount());
        }
    }
}