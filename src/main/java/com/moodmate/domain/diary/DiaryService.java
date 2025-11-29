package com.moodmate.domain.diary;

import com.moodmate.config.encryption.EncryptionKeyService;
import com.moodmate.config.encryption.EncryptionService;
import com.moodmate.domain.diary.dto.*;
import com.moodmate.domain.diary.entity.Diary;
import com.moodmate.domain.diary.entity.DiaryEmotion;
import com.moodmate.domain.diary.repository.DiaryRepository;
import com.moodmate.domain.emotion.Emotion;
import com.moodmate.domain.feedback.entity.AiFeedback;
import com.moodmate.domain.feedback.repository.AiFeedbackRepository;
import com.moodmate.domain.user.entity.User;
import com.moodmate.domain.emotion.EmotionRepository;
import com.moodmate.domain.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final EmotionRepository emotionRepository;
    private final UserRepository userRepository;

    private final AiFeedbackRepository aiFeedbackRepository;

    private final EncryptionService encryptionService;

    private final EncryptionKeyService keyService;

    private final DiaryMapper diaryMapper;

    @Transactional
    public Long saveDiary(Long userId, @Valid DiaryRequest dto) {
        // 작성자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        try {
            String dek = keyService.decryptDek(user.getEncryptedDek());

            // 사용자 키로 암호화
            String encryptedContent = encryptionService.encrypt(dto.getContent(), dek);

            // Diary 생성
            Diary diary = new Diary(encryptedContent, dto.getDate(), user);

            // 감정 리스트 처리
            for (EmotionDto e : dto.getEmotions()) {
                Emotion emotion = emotionRepository.findByName(e.name())
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 감정입니다: " + e.name()));

                DiaryEmotion diaryEmotion = new DiaryEmotion(emotion, e.intensity());
                diary.addDiaryEmotion(diaryEmotion); // 양방향 연결
            }

            // 저장 (Cascade로 DiaryEmotion까지 저장됨)
            diaryRepository.save(diary);

            return diary.getId();

        } catch(Exception e) {
            throw new RuntimeException("일기 암호화 중 오류 발생", e);
        }
    }

    public DiaryResponse getDiaryByDate(Long userId, LocalDate date) {
        Diary diary = diaryRepository.findByUserIdAndDate(userId, date)
                .orElseThrow(() -> new IllegalArgumentException("해당 날짜의 일기가 없습니다."));

        // 작성자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        try {
            String dek = keyService.decryptDek(user.getEncryptedDek());
            return diaryMapper.toResponseDto(diary, dek);
        } catch (Exception e) {
            throw new RuntimeException("dek 복호화 중 오류 발생");
        }
    }

    public List<DiarySummaryOnlyResponse> getDiariesByPeriodSummary(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Diary> diaries = diaryRepository.findByUserIdAndDateBetween(userId, startDate, endDate);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        try {
            String dek = keyService.decryptDek(user.getEncryptedDek());

            return diaries.stream()
                    .map(diary -> {
                        // 해당 일기의 가장 최근 피드백 조회
                        Optional<AiFeedback> feedbackOpt = aiFeedbackRepository.findLatestByDiaryId(diary.getId());

                        String summary = "";
                        if (feedbackOpt.isPresent()) {
                            try {
                                summary = encryptionService.decrypt(feedbackOpt.get().getSummary(), dek);
                            } catch (Exception e) {
                                log.error("피드백 요약 복호화 실패 - 일기 ID: {}", diary.getId(), e);
                            }
                        }

                        // 감정 정보 추출
                        List<EmotionDto> emotions = diary.getDiaryEmotions().stream()
                                .map(de -> new EmotionDto(
                                        de.getEmotion().getName(),
                                        de.getIntensity()))
                                .toList();

                        return new DiarySummaryOnlyResponse(
                                diary.getDate(),
                                diary.getId(),
                                summary,
                                emotions
                        );
                    })
                    // summary 또는 emotions가 있으면 포함 (둘 다 없으면 제외)
                    .filter(res -> (res.summary() != null && !res.summary().isBlank())
                            || (res.emotions() != null && !res.emotions().isEmpty()))
                    .toList();

        } catch (Exception e) {
            throw new RuntimeException("dek 복호화 중 오류 발생");
        }
    }


    /**
     * 기간별 일기 조회 - 감정만 (날짜, ID, 감정만, 내용 없음)
     * summary와 동일하지만 의미상 분리
     */
    public List<DiaryMonthSummaryResponse> getDiariesByPeriodEmotion(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Diary> diaries = diaryRepository.findByUserIdAndDateBetween(userId, startDate, endDate);

        return diaries.stream()
                .map(diary -> new DiaryMonthSummaryResponse(
                        diary.getDate(),
                        diary.getId(),
                        diary.getDiaryEmotions().stream()
                                .map(de -> new EmotionDto(
                                        de.getEmotion().getName(),
                                        de.getIntensity()))
                                .toList()
                ))
                .toList();
    }

    /**
     * 기간별 일기 조회 - 전체 내용 포함
     */
    public List<DiaryResponse> getDiariesByPeriodFull(Long userId, LocalDate startDate, LocalDate endDate) {
        // 작성자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        List<Diary> diaries = diaryRepository.findByUserIdAndDateBetween(userId, startDate, endDate);


        try {
            String dek = keyService.decryptDek(user.getEncryptedDek());
            return diaryMapper.toResponseDtoList(diaries, dek);
        } catch (Exception e) {
            throw new RuntimeException("dek 복호화 중 오류 발생");
        }
    }

    /**
     * @deprecated 월별 조회는 기간별 조회로 대체되었습니다. getDiariesByPeriodSummary 사용을 권장합니다.
     */
    @Deprecated
    public List<DiaryMonthSummaryResponse> getDiarySummariesByMonth(Long userId, YearMonth yearMonth) {
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        return getDiariesByPeriodEmotion(userId, start, end);
    }

    @Transactional
    public void updateDiary(Long diaryId, DiaryRequest dto, Long userId) throws AccessDeniedException {
        // 일기 조회
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("일기를 찾을 수 없습니다."));

        if (diary.getUser().getId() != userId) {
            throw new AccessDeniedException("해당 일기에 접근할 수 없습니다.");
        }

        // 작성자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        try {
            String dek = keyService.decryptDek(user.getEncryptedDek());

            // 일기 내용, 날짜 변경
            diary.setContent(encryptionService.encrypt(dto.getContent(), dek));
            diary.setDate(dto.getDate());

            // 기존 감정 초기화
            diary.getDiaryEmotions().clear();

            // 새 감정들 추가
            for (EmotionDto e : dto.getEmotions()) {
                Emotion emotion = emotionRepository.findByName(e.name())
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 감정입니다: " + e.name()));
                DiaryEmotion de = new DiaryEmotion(emotion, e.intensity());
                diary.addDiaryEmotion(de);
            }
        } catch (Exception e) {
            throw new RuntimeException("일기 암호화 중 오류");
        }
    }

    @Transactional
    public void deleteDiary(Long diaryId, Long userId) throws AccessDeniedException {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("일기를 찾을 수 없습니다."));

        if (diary.getUser().getId() != userId) {
            throw new AccessDeniedException("해당 일기에 접근할 수 없습니다.");
        }

        diaryRepository.delete(diary);
    }

    public Integer getDiaryCountByPeriod(Long userId, LocalDate startDate, LocalDate endDate) {
        return diaryRepository.countByUserIdAndDateBetween(userId, startDate, endDate);
    }

}