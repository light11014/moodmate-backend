package com.moodmate.domain.tracking.dayOfWeek;

import com.moodmate.domain.tracking.TrackingMetaBase;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record WeekMeta(
        Long userId,

        LocalDate startDate,

        LocalDate endDate,

        @Schema(description = "데이터 개수", example = "7")
        long totalRecords,

        @Schema(description = "조회된 일기 수", example = "50")
        long totalDiaryCount,

        LocalDateTime generatedAt)  implements TrackingMetaBase {}
