package com.moodmate.domain.tracking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "감정 비율 조회 응답")
public record EmotionRatioResponse(
        @Schema(description = "메타 데이터")
        RatioMeta meta,

        @Schema(description = "감정 비율 데이터")
        List<EmotionRatioDto> data
) {
        public record RatioMeta(Long userId, LocalDate startDate, LocalDate endDate,
                                long totalIntensity, LocalDateTime generatedAt)
                implements TrackingMetaBase {}

        public record EmotionRatioDto(
                @Schema(description = "감정 이름", example = "기쁨")
                String emotion,

                @Schema(description = "감정 강도 비율(0 ~ 1)", example = "0.3")
                double ratio
        ) {
        }
}
