package com.moodmate.domain.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record DiaryMonthSummaryResponse(
        @Schema(description = "일기 날짜", example = "2025-04-08")
        LocalDate date,

        @Schema(description = "일기 ID", example = "123")
        Long diaryId,

        @Schema(description = "감정 목록")
        List<EmotionDto> emotions
) {}