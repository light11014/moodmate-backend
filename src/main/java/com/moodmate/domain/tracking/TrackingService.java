package com.moodmate.domain.tracking;

import com.moodmate.domain.diary.repository.DiaryEmotionRepository;
import com.moodmate.domain.tracking.dto.frequency.EmotionFrequencyDto;
import com.moodmate.domain.tracking.dto.frequency.EmotionFrequencyResponse;
import com.moodmate.domain.tracking.dto.frequency.FrequencyMeta;
import com.moodmate.domain.tracking.dto.ratio.EmotionRatioDto;
import com.moodmate.domain.tracking.dto.ratio.EmotionRatioResponse;
import com.moodmate.domain.tracking.dto.ratio.RatioMeta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrackingService {
    private final DiaryEmotionRepository diaryEmotionRepository;

    public EmotionFrequencyResponse getEmotionFrequency(Long userId, LocalDate startDate, LocalDate endDate) {
        validateParameters(userId, startDate, endDate);

        List<EmotionFrequencyDto> data = diaryEmotionRepository.countEmotionsByPeriod(userId, startDate, endDate);

        FrequencyMeta meta = new FrequencyMeta(
                userId,
                startDate,
                endDate,
                data.size(),
                LocalDateTime.now()
        );

        return new EmotionFrequencyResponse(meta, data);
    }



    public EmotionRatioResponse getEmotionRatio(Long userId, LocalDate startDate, LocalDate endDate) {
        validateParameters(userId, startDate, endDate);

        List<Object[]> result = diaryEmotionRepository.sumEmotionIntensityByPeriod(userId, startDate, endDate);

        long total = result.stream()
                .mapToLong(r -> ((Number) r[1]).longValue())
                .sum();

        List<EmotionRatioDto> data = result.stream()
                .map(r -> new EmotionRatioDto(
                        (String) r[0],
                        total == 0 ? 0.0 : ((Number) r[1]).doubleValue() / total
                ))
                .toList();

        RatioMeta meta = new RatioMeta(
                userId,
                startDate,
                endDate,
                data.size(),
                LocalDateTime.now()
        );

        return new EmotionRatioResponse(meta, data);
    }

    private static void validateParameters(Long userId, LocalDate startDate, LocalDate endDate) {
        if (userId == null || startDate == null || endDate == null) {
            throw new IllegalArgumentException("필수 파라미터를 입력해주세요");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("기간 설정이 올바르지 않습니다");
        }
    }
}
