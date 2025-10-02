package com.moodmate.domain.diary;

import com.moodmate.domain.diary.dto.DiaryMonthSummaryResponse;
import com.moodmate.domain.diary.dto.DiaryRequest;
import com.moodmate.domain.diary.dto.DiaryResponse;
import com.moodmate.domain.diary.dto.EmotionDto;
import com.moodmate.domain.diary.entity.Diary;
import com.moodmate.domain.diary.entity.DiaryEmotion;
import com.moodmate.domain.diary.repository.DiaryRepository;
import com.moodmate.domain.emotion.Emotion;
import com.moodmate.domain.user.entity.User;
import com.moodmate.domain.emotion.EmotionRepository;
import com.moodmate.domain.user.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final EmotionRepository emotionRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long saveDiary(Long userId, @Valid DiaryRequest dto) {
        // 작성자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // Diary 생성
        Diary diary = new Diary(dto.getContent(), dto.getDate(), user);

        // 감정 리스트 처리
        for (EmotionDto e : dto.getEmotions()) {
            Emotion emotion = emotionRepository.findByName(e.name())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 감정입니다: " + e.name()));

            DiaryEmotion diaryEmotion = new DiaryEmotion(emotion, e.intensity());
            diary.addDiaryEmotion(diaryEmotion); // 양방향 연결
        }

        // 저장 (Cascade로 DiaryEmotion까지 저장됨)
        user.addDiary(diary);
        diaryRepository.save(diary);

        return diary.getId();
    }

    public DiaryResponse getDiaryByDate(Long userId, LocalDate date) {
        Diary diary = diaryRepository.findByUserIdAndDate(userId, date)
                .orElseThrow(() -> new IllegalArgumentException("해당 날짜의 일기가 없습니다."));
        return new DiaryResponse(diary);
    }

    public List<DiaryMonthSummaryResponse> getDiarySummariesByMonth(Long userId, YearMonth yearMonth) {
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<Diary> diaries = diaryRepository.findByUserIdAndDateBetween(userId, start, end);

        return diaries.stream()
                .map(diary -> new DiaryMonthSummaryResponse(
                        diary.getDate(),
                        diary.getDiaryEmotions().stream()
                                .map(de -> new EmotionDto(
                                        de.getEmotion().getName(),
                                        de.getIntensity()))
                                .toList()
                ))
                .toList();
    }

    @Transactional
    public void updateDiary(Long diaryId, DiaryRequest dto, Long userId) throws AccessDeniedException {
        // 일기 조회
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("일기를 찾을 수 없습니다."));

        if (diary.getUser().getId() != userId) {
            throw new AccessDeniedException("해당 일기에 접근할 수 없습니다.");
        }

        // 일기 내용, 날짜 변경
        diary.setContent(dto.getContent());
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
    }

    @Transactional
    public void deleteDiary(Long diaryId, Long userId) throws AccessDeniedException {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("일기를 찾을 수 없습니다."));

        if (diary.getUser().getId() != userId) {
            throw new AccessDeniedException("해당 일기에 접근할 수 없습니다.");
        }

        diaryRepository.delete(diary); // DiaryEmotion도 함께 삭제됨
    }

}
