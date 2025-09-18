package com.moodmate.domain.tracking.dto.frequency;

import com.moodmate.domain.tracking.dto.TrackingMetaBase;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record FrequencyMeta(
    Long userId,

    LocalDate startDate,

    LocalDate endDate,

    @Schema(description = "감정 개수", example = "1")
    long totalRecords,

    LocalDateTime generatedAt)  implements TrackingMetaBase {}
