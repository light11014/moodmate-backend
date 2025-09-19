package com.moodmate.domain.tracking.dto.trend;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record TimeLinePointDto(
        @Schema(description = "날짜", example = "2025-09-01")
        LocalDate date,

        @Schema(description = "감정 강도(1 ~ 5)", example = "3")
        int intensity
) {}