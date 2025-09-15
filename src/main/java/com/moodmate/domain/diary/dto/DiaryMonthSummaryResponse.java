package com.moodmate.domain.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class DiaryMonthSummaryResponse {
    @Schema(description = "일기 날짜", example = "2025-04-08")
    private LocalDate date;

    private List<EmotionDto> emotions;
}
