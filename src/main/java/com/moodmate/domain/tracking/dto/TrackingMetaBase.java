package com.moodmate.domain.tracking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "공통 메타 데이터")
public interface TrackingMetaBase{
        @Schema(description = "사용자 ID", example = "42")
        Long userId();

        @Schema(description = "조회 시작일", example = "2025-09-01")
        LocalDate startDate();

        @Schema(description = "조회 종료일", example = "2025-09-05")
        LocalDate endDate();

        @Schema(description = "응답 생성 시각")
        LocalDateTime generatedAt();
}