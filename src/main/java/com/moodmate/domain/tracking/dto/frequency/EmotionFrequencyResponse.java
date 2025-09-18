package com.moodmate.domain.tracking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record EmotionFrequencyResponse(
        @Schema(description = "메타 데이터")
        TrackingMeta meta,

        @Schema(description = "감정 빈도 데이터")
        List<EmotionFrequencyDto> data
) {
        public record EmotionFrequencyDto(
                @Schema(description = "감정 이름", example = "기쁨")
                String emotion,

                @Schema(description = "기간별 감정 횟수", example = "10")
                Long count
        ) {
        }
}
