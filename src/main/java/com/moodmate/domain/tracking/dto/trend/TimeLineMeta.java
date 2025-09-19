package com.moodmate.domain.tracking.dto.trend;

import com.moodmate.domain.tracking.dto.TrackingMetaBase;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TimeLineMeta(
        Long userId,

       LocalDate startDate,

       LocalDate endDate,

       @Schema(description = "조회한 감정", example = "[\'기쁨\', \'슬픔\']")
       List<String> selectedEmotions,

       LocalDateTime generatedAt)  implements TrackingMetaBase {}
