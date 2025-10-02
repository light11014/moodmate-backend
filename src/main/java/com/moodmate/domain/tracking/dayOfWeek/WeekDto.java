package com.moodmate.domain.tracking.dayOfWeek;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record WeekDto(
        @Schema(description = "요일", example = "MONDAY")
        String dayOfWeek,

        @Schema(description = "요일(한글)", example = "월요일")
        String dayOfWeekKo,

        @Schema(description = "해당 요일의 총 일기 개수", example = "8")
        Integer diaryCount,

        @Schema(description = "해당 요일의 총 감정 개수", example = "3")
        Integer emotionCount,

        @Schema(description = "해당 요일의 감정 분포")
        List<EmotionStatistics> emotions
) {
}
