package com.moodmate.domain.tracking.dto.frequency;

import io.swagger.v3.oas.annotations.media.Schema;

public record FrequencyDto(
        @Schema(description = "감정 이름", example = "기쁨")
        String emotion,

        @Schema(description = "기간별 감정 횟수", example = "10")
        Long count
) {
}
