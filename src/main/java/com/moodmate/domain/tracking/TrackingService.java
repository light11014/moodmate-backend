package com.moodmate.domain.tracking;

import com.moodmate.domain.diary.repository.DiaryEmotionRepository;
import com.moodmate.domain.emotion.Emotion;
import com.moodmate.domain.emotion.EmotionRepository;
import com.moodmate.domain.tracking.dto.frequency.FrequencyDto;
import com.moodmate.domain.tracking.dto.frequency.EmotionFrequencyResponse;
import com.moodmate.domain.tracking.dto.frequency.FrequencyMeta;
import com.moodmate.domain.tracking.dto.ratio.RatioDto;
import com.moodmate.domain.tracking.dto.ratio.EmotionRatioResponse;
import com.moodmate.domain.tracking.dto.ratio.RatioMeta;
import com.moodmate.domain.tracking.dto.trend.TimeLineDto;
import com.moodmate.domain.tracking.dto.trend.TimeLineMeta;
import com.moodmate.domain.tracking.dto.trend.EmotionTrendResponse;
import com.moodmate.domain.tracking.dto.trend.TimeLinePointDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrackingService {
    private final DiaryEmotionRepository diaryEmotionRepository;
    private final EmotionRepository emotionRepository;

    public EmotionFrequencyResponse getEmotionFrequency(Long userId, LocalDate startDate, LocalDate endDate) {
        validateParameters(userId, startDate, endDate);

        List<FrequencyDto> data = diaryEmotionRepository.countEmotionsByPeriod(userId, startDate, endDate);

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

        List<RatioDto> data = result.stream()
                .map(r -> new RatioDto(
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

    public EmotionTrendResponse getEmotionTrend(Long userId, LocalDate startDate, LocalDate endDate, List<String> selectedEmotions) {
        validateParameters(userId, startDate, endDate);

        // DB 검색
        List<Object[]> rows = diaryEmotionRepository.findEmotionTimeline(
                userId, startDate, endDate,
                (selectedEmotions == null || selectedEmotions.isEmpty()) ? null : selectedEmotions
        );

        // 감정별 그룹핑
        Map<String, List<TimeLinePointDto>> grouped =
                rows.stream()
                        .collect(Collectors.groupingBy(
                                r -> (String) r[0],
                                LinkedHashMap::new,
                                Collectors.mapping(
                                        r -> new TimeLinePointDto(
                                                (LocalDate) r[1],
                                                ((Number) r[2]).intValue()
                                        ),
                                        Collectors.toList()
                                )
                        ));

        // DTO 변환
        List<TimeLineDto> data = grouped.entrySet().stream()
                .map(e -> new TimeLineDto(
                        e.getKey(),
                        e.getValue().stream()
                                .sorted(Comparator.comparing(TimeLinePointDto::date))
                                .toList()
                ))
                .toList();

        // Meta 생성
        TimeLineMeta meta = new TimeLineMeta(
                userId,
                startDate,
                endDate,
                (selectedEmotions == null || selectedEmotions.isEmpty())  ? emotionRepository.findAll().stream().map(Emotion::getName).toList() : selectedEmotions,
                LocalDateTime.now()
        );

        return new EmotionTrendResponse(meta, data);
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
