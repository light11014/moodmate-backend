package com.moodmate.domain.tracking.ratio;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record RatioMeta(
        Long userId,

        LocalDate startDate,

        LocalDate endDate,

        @Schema(description = "데이터 개수", example = "2")
        long totalRecords,

        LocalDateTime generatedAt)
{}
