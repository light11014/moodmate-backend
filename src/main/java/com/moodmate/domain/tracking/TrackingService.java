package com.moodmate.domain.tracking;

import com.moodmate.domain.diary.repository.DiaryEmotionRepository;
import com.moodmate.domain.emotion.EmotionRepository;
import com.moodmate.domain.tracking.dto.EmotionFrequency;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrackingService {
    private final DiaryEmotionRepository diaryEmotionRepository;

    public List<EmotionFrequency> getEmotionFrequency(Long userId, LocalDate startDate, LocalDate endDate) {
        if (userId == null || startDate == null || endDate == null) {
            throw new IllegalArgumentException("필수 파라미터를 입력해주세요");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("기간 설정이 올바르지 않습니다");
        }
        return diaryEmotionRepository.countEmotionsByPeriod(userId, startDate, endDate);
    }
}
