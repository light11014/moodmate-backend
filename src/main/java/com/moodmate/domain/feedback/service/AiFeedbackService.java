package com.moodmate.domain.feedback.service;

import com.moodmate.config.encryption.EncryptionKeyService;
import com.moodmate.config.encryption.EncryptionService;
import com.moodmate.domain.diary.entity.Diary;
import com.moodmate.domain.diary.repository.DiaryRepository;
import com.moodmate.domain.feedback.dto.*;
import com.moodmate.domain.feedback.entity.AiFeedback;
import com.moodmate.domain.feedback.entity.DailyFeedbackUsage;
import com.moodmate.domain.feedback.repository.AiFeedbackRepository;
import com.moodmate.domain.feedback.repository.DailyFeedbackUsageRepository;
import com.moodmate.domain.user.entity.User;
import com.moodmate.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AiFeedbackService {

    private final AiFeedbackRepository aiFeedbackRepository;
    private final DailyFeedbackUsageRepository dailyUsageRepository;
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;

    private final EncryptionService encryptionService;

    private final EncryptionKeyService keyService;

    private final FeedbackMapper feedbackMapper;

    private static final int DAILY_FEEDBACK_LIMIT = 2;

    /**
     * 피드백 생성 (수정된 메소드)
     * diaryId를 별도 파라미터로 받음
     */
    public FeedbackResponse createFeedback(Long userId, Long diaryId, FeedbackStyleRequest request) throws AccessDeniedException {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 일기 조회 및 권한 확인
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("일기를 찾을 수 없습니다."));

        if (!Objects.equals(userId, diary.getUser().getId())) {
            throw new AccessDeniedException("해당 일기에 대한 권한이 없습니다.");
        }

        // 기존 피드백이 있다면 삭제 (덮어쓰기)
        aiFeedbackRepository.findLatestByDiaryId(diaryId).ifPresent(existingFeedback -> {
            log.info("기존 피드백 삭제 - 일기 ID: {}, 피드백 ID: {}", diaryId, existingFeedback.getId());
            aiFeedbackRepository.delete(existingFeedback);
        });

        // 일일 사용량 확인 및 업데이트
        checkAndUpdateDailyUsage(userId);

        try {
            String dek = keyService.decryptDek(user.getEncryptedDek());

            // 일기 내용 복호화
            String content = encryptionService.decrypt(diary.getContent(), dek);

            // AI 분석 실행
            String summary = geminiService.generateSummary(content);
            String response = geminiService.generateFeedback(content, request.feedbackStyle());

            // 피드백 저장
            AiFeedback feedback = AiFeedback.builder()
                    .user(user)
                    .diary(diary)
                    .summary(encryptionService.encrypt(summary, dek))
                    .response(encryptionService.encrypt(response, dek))
                    .feedbackStyle(request.feedbackStyle())
                    .requestedAt(LocalDateTime.now())
                    .build();

            aiFeedbackRepository.save(feedback);
            log.info("피드백 생성 완료 - 사용자: {}, 일기: {}", userId, diaryId);

            return new FeedbackResponse(feedback);

        } catch (Exception e) {
            throw new RuntimeException("AI 피드백 생성 중 오류");
        }
    }

    @Transactional(readOnly = true)
    public FeedbackResponse getFeedback(Long userId, Long diaryId) throws AccessDeniedException {
        // 일기 조회 및 권한 확인
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("일기를 찾을 수 없습니다."));

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));


        if (!Objects.equals(diary.getUser().getId(), userId)) {
            throw new AccessDeniedException("해당 일기에 대한 권한이 없습니다.");
        }

        // 가장 최근 피드백 조회
        AiFeedback feedback = aiFeedbackRepository.findLatestByDiaryId(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 일기에 대한 피드백이 없습니다."));


        // AI피드백 복호화
        try {
            String dek = keyService.decryptDek(user.getEncryptedDek());
            return feedbackMapper.toResponseDto(feedback, dek);
        } catch (Exception e) {
            throw new RuntimeException("AI 피드백 조회 중 오류");
        }
    }

    @Transactional(readOnly = true)
    public FeedbackHistoryResponse getFeedbackHistory(Long userId, LocalDate startDate, LocalDate endDate) {
        List<AiFeedback> feedbacks = aiFeedbackRepository.findByUserIdAndDateRange(userId, startDate, endDate);

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        try {
            String dek = keyService.decryptDek(user.getEncryptedDek());
            return feedbackMapper.toFeedbackHistoryResponse(feedbacks, dek, startDate, endDate);
        } catch (Exception e) {
            throw new RuntimeException("AI 피드백 history 조회 중 오류");
        }
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

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 복호화하면서 요약들을 결합
        try {
            String dek = keyService.decryptDek(user.getEncryptedDek());

            String combinedSummaries = feedbacks.stream()
                    .map(feedback -> {
                        try {
                            return encryptionService.decrypt(feedback.getSummary(), dek);
                        } catch (Exception e) {
                            log.error("요약 복호화 실패 - 피드백 ID: {}", feedback.getId(), e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .filter(summary -> !summary.trim().isEmpty())
                    .collect(Collectors.joining("\n\n"));

            if (combinedSummaries.trim().isEmpty()) {
                throw new IllegalArgumentException("분석할 요약 데이터가 없습니다.");
            }

            // 이제 복호화된 텍스트로 AI 분석
            String periodSummary = geminiService.generatePeriodSummary(
                    combinedSummaries, startDate, endDate
            );
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
     * 피드백 삭제
     */
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

        // 피드백 삭제
        aiFeedbackRepository.delete(feedback);
        log.info("피드백 삭제 완료 - 사용자: {}, 일기: {}, 피드백: {}", userId, diaryId, feedback.getId());
    }

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