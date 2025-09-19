package com.moodmate.domain.emotion.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record EmotionRequest(
        @Schema(description = "감정 이름", example = "기쁨")
        String name
) {
}
