package com.moodmate.domain.tracking.dto.frequency;

import com.moodmate.domain.tracking.dto.TrackingMetaBase;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record FrequencyMeta(
    Long userId,

    LocalDate startDate,

    LocalDate endDate,

    @Schema(description = "데이터 수", example = "2")
    long totalRecords,

    LocalDateTime generatedAt)  implements TrackingMetaBase {}
