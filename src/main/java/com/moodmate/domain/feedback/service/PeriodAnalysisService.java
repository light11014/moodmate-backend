package com.moodmate.domain.feedback.service;

import com.moodmate.config.encryption.EncryptionKeyService;
import com.moodmate.config.encryption.EncryptionService;
import com.moodmate.domain.diary.entity.Diary;
import com.moodmate.domain.diary.repository.DiaryRepository;
import com.moodmate.domain.feedback.dto.*;
import com.moodmate.domain.feedback.entity.PeriodAnalysis;
import com.moodmate.domain.feedback.repository.PeriodAnalysisRepository;
import com.moodmate.domain.user.UserRepository;
import com.moodmate.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PeriodAnalysisService {

    private final PeriodAnalysisRepository periodAnalysisRepository;
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;
    private final EncryptionService encryptionService;
    private final EncryptionKeyService keyService;

    /**
     * 종합 피드백 생성 및 저장
     */
    @Transactional
    public PeriodAnalysisDetailResponse createPeriodAnalysis(Long userId, CreatePeriodAnalysisRequest request) throws Exception {
        request.validate();

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        log.info("종합 피드백 생성 시작 - 사용자: {}, 기간: {} ~ {}", userId, startDate, endDate);

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. 기존 분석 확인
        Optional<PeriodAnalysis> existingAnalysis =
                periodAnalysisRepository.findLatestByUserIdAndPeriod(userId, startDate, endDate);

        if (existingAnalysis.isPresent() && !request.isOverwrite()) {
            // 기존 분석이 있고 덮어쓰기가 아니면 기존 분석 반환
            log.info("기존 종합 피드백 반환 - ID: {}", existingAnalysis.get().getId());
            return convertToDetailResponse(existingAnalysis.get(), user);
        }

        // 3. 해당 기간의 일기들 직접 조회 (DiaryRepository 사용)
        List<Diary> diaries = diaryRepository.findByUserIdAndDateBetween(userId, startDate, endDate);

        if (diaries.isEmpty()) {
            throw new IllegalArgumentException("해당 기간에 분석할 일기 데이터가 없습니다.");
        }

        // 4. 복호화 키 준비
        String dek = keyService.decryptDek(user.getEncryptedDek());

        // 5. 일기 내용들 결합 (날짜별로)
        String combinedDiaryContents = diaries.stream()
                .map(diary -> {
                    try {
                        String diaryContent = encryptionService.decrypt(diary.getContent(), dek);
                        LocalDate diaryDate = diary.getDate();
                        return String.format("[%s]\n%s", diaryDate, diaryContent);
                    } catch (Exception e) {
                        log.error("일기 복호화 실패 - 일기 ID: {}", diary.getId(), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(content -> !content.trim().isEmpty())
                .collect(Collectors.joining("\n\n---\n\n"));

        if (combinedDiaryContents.trim().isEmpty()) {
            throw new IllegalArgumentException("분석할 일기 데이터가 없습니다.");
        }

        // 6. AI 종합 분석 생성 (일기 내용 기반)
        String periodSummary = geminiService.generatePeriodSummary(combinedDiaryContents, startDate, endDate);
        String recommendations = geminiService.generateRecommendations(combinedDiaryContents);

        // 7. 암호화하여 저장
        String encryptedPeriodSummary = encryptionService.encrypt(periodSummary, dek);
        String encryptedRecommendations = encryptionService.encrypt(recommendations, dek);
        String encryptedCombinedContents = encryptionService.encrypt(combinedDiaryContents, dek);

        // 8. 기존 분석 삭제 (덮어쓰기 모드)
        if (existingAnalysis.isPresent()) {
            log.info("기존 종합 피드백 삭제 (덮어쓰기) - ID: {}", existingAnalysis.get().getId());
            periodAnalysisRepository.delete(existingAnalysis.get());
        }

        // 9. 새 분석 저장
        PeriodAnalysis analysis = PeriodAnalysis.builder()
                .user(user)
                .startDate(startDate)
                .endDate(endDate)
                .analyzedDiaryCount(diaries.size())
                .periodSummary(encryptedPeriodSummary)
                .recommendations(encryptedRecommendations)
                .combinedSummaries(encryptedCombinedContents)
                .build();

        periodAnalysisRepository.save(analysis);

        log.info("종합 피드백 생성 완료 - 사용자: {}, 분석 ID: {}, 일기 수: {}",
                userId, analysis.getId(), diaries.size());

        return convertToDetailResponse(analysis, user);
    }

    /**
     * 종합 피드백 상세 조회
     */
    public PeriodAnalysisDetailResponse getPeriodAnalysis(Long userId, Long analysisId) throws AccessDeniedException {
        // 분석 조회 및 권한 확인
        PeriodAnalysis analysis = periodAnalysisRepository.findByIdAndUserId(analysisId, userId)
                .orElseThrow(() -> new IllegalArgumentException("종합 피드백을 찾을 수 없습니다."));

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return convertToDetailResponse(analysis, user);
    }

    /**
     * 사용자의 모든 종합 피드백 목록 조회
     */
    public PeriodAnalysisListResponse getAllPeriodAnalyses(Long userId) {
        List<PeriodAnalysis> analyses = periodAnalysisRepository.findAllByUserId(userId);

        List<PeriodAnalysisListItem> items = analyses.stream()
                .map(this::convertToListItem)
                .toList();

        return new PeriodAnalysisListResponse(items);
    }

    /**
     * 특정 기간의 종합 피드백 조회 (가장 최근 것)
     */
    public Optional<PeriodAnalysisDetailResponse> getPeriodAnalysisByPeriod(
            Long userId, LocalDate startDate, LocalDate endDate) {

        Optional<PeriodAnalysis> analysis =
                periodAnalysisRepository.findLatestByUserIdAndPeriod(userId, startDate, endDate);

        if (analysis.isEmpty()) {
            return Optional.empty();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return Optional.of(convertToDetailResponse(analysis.get(), user));
    }

    /**
     * 종합 피드백 삭제
     */
    @Transactional
    public void deletePeriodAnalysis(Long userId, Long analysisId) throws AccessDeniedException {
        // 분석 조회 및 권한 확인
        PeriodAnalysis analysis = periodAnalysisRepository.findByIdAndUserId(analysisId, userId)
                .orElseThrow(() -> new IllegalArgumentException("종합 피드백을 찾을 수 없습니다."));

        periodAnalysisRepository.delete(analysis);
        log.info("종합 피드백 삭제 완료 - 사용자: {}, 분석 ID: {}", userId, analysisId);
    }

    /**
     * PeriodAnalysis를 상세 응답 DTO로 변환 (복호화 포함)
     */
    private PeriodAnalysisDetailResponse convertToDetailResponse(PeriodAnalysis analysis, User user) {
        try {
            String dek = keyService.decryptDek(user.getEncryptedDek());

            return PeriodAnalysisDetailResponse.builder()
                    .analysisId(analysis.getId())
                    .startDate(analysis.getStartDate())
                    .endDate(analysis.getEndDate())
                    .analyzedDiaryCount(analysis.getAnalyzedDiaryCount())
                    .periodSummary(encryptionService.decrypt(analysis.getPeriodSummary(), dek))
                    .recommendations(encryptionService.decrypt(analysis.getRecommendations(), dek))
                    .createdAt(analysis.getCreated_at())
                    .build();
        } catch (Exception e) {
            log.error("종합 피드백 복호화 중 오류 - 분석 ID: {}", analysis.getId(), e);
            throw new RuntimeException("종합 피드백 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * PeriodAnalysis를 목록 아이템 DTO로 변환
     */
    private PeriodAnalysisListItem convertToListItem(PeriodAnalysis analysis) {
        return PeriodAnalysisListItem.builder()
                .analysisId(analysis.getId())
                .startDate(analysis.getStartDate())
                .endDate(analysis.getEndDate())
                .analyzedDiaryCount(analysis.getAnalyzedDiaryCount())
                .createdAt(analysis.getCreated_at())
                .build();
    }
}
