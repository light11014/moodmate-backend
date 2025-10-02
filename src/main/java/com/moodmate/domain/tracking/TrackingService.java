package com.moodmate.domain.tracking;

import com.moodmate.domain.diary.repository.DiaryEmotionRepository;
import com.moodmate.domain.diary.repository.DiaryRepository;
import com.moodmate.domain.emotion.Emotion;
import com.moodmate.domain.emotion.EmotionRepository;
import com.moodmate.domain.tracking.dayOfWeek.*;
import com.moodmate.domain.tracking.frequency.FrequencyDto;
import com.moodmate.domain.tracking.frequency.EmotionFrequencyResponse;
import com.moodmate.domain.tracking.frequency.FrequencyMeta;
import com.moodmate.domain.tracking.ratio.RatioDto;
import com.moodmate.domain.tracking.ratio.EmotionRatioResponse;
import com.moodmate.domain.tracking.ratio.RatioMeta;
import com.moodmate.domain.tracking.trend.TimeLineDto;
import com.moodmate.domain.tracking.trend.TimeLineMeta;
import com.moodmate.domain.tracking.trend.EmotionTrendResponse;
import com.moodmate.domain.tracking.trend.TimeLinePointDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrackingService {
    private final DiaryEmotionRepository diaryEmotionRepository;
    private final EmotionRepository emotionRepository;
    private final DiaryRepository diaryRepository;

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

    public DayOfWeekEmotionResponse getEmotionsByDayOfWeek(long userId, LocalDate startDate, LocalDate endDate) {
        validateParameters(userId, startDate, endDate);

        Integer totalDiaryCount = diaryRepository.countByUserIdAndDateBetween(userId, startDate, endDate);

        // DB에서 감정 가져오기
        List<DayOfWeekEmotionProjection> projections = diaryEmotionRepository.findDayOfWeekEmotionStats(userId, startDate, endDate);

        // null 체크 추가
        if (projections == null) {
            projections = List.of();
        }

        // 요일별로 그룹화
        Map<DayOfWeek, List<DayOfWeekEmotionProjection>> groupedByDay = projections.stream()
                .collect(Collectors.groupingBy(p -> DayOfWeekConverter.fromPostgresValue(p.getDayOfWeek())));

        // 월요일부터 일요일까지 순서대로 WeekDto 생성
        List<WeekDto> weekData = Arrays.stream(DayOfWeek.values())
                .map(dayOfWeek -> createWeekDto(dayOfWeek, groupedByDay.getOrDefault(dayOfWeek, List.of())))
                .collect(Collectors.toList());

        WeekMeta meta = new WeekMeta(
                userId,
                startDate,
                endDate,
                weekData.size(),
                totalDiaryCount,
                LocalDateTime.now()
        );

        return new DayOfWeekEmotionResponse(meta, weekData);
    }

    private void validateParameters(Long userId, LocalDate startDate, LocalDate endDate) {
        if (userId == null || startDate == null || endDate == null) {
            throw new IllegalArgumentException("필수 파라미터를 입력해주세요");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("기간 설정이 올바르지 않습니다");
        }
    }

    private WeekDto createWeekDto(DayOfWeek dayOfWeek, List<DayOfWeekEmotionProjection> projections) {
        if (projections.isEmpty()) {
            return new WeekDto(
                    dayOfWeek.name(),
                    DayOfWeekConverter.getKoreanName(dayOfWeek),
                    0,
                    0,
                    List.of()
            );
        }

        int diaryCount = projections.get(0).getDiaryCount().intValue();
        long totalEmotionCount = projections.stream().mapToLong(DayOfWeekEmotionProjection::getCount).sum();

        List<EmotionStatistics> emotionStats = projections.stream()
                .map(p -> new EmotionStatistics(
                        p.getEmotionName(),
                        p.getCount().intValue(),
                        Math.round((p.getCount() * 100.0 / totalEmotionCount) * 10.0) / 10.0,
                        Math.round(p.getAvgIntensity() * 10.0) / 10.0
                ))
                .collect(Collectors.toList());

        return new WeekDto(
                dayOfWeek.name(),
                DayOfWeekConverter.getKoreanName(dayOfWeek),
                diaryCount,
                emotionStats.size(),
                emotionStats
        );
    }
}
