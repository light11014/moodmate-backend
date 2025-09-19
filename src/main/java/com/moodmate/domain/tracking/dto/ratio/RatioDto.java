package com.moodmate.domain.tracking.dto.ratio;

import io.swagger.v3.oas.annotations.media.Schema;

public record RatioDto(
        @Schema(description = "감정 이름", example = "기쁨")
        String emotion,

        @Schema(description = "감정 강도 비율(0 ~ 1)", example = "1.0")
        double ratio
) {
}
