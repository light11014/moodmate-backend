package com.moodmate.domain.emotion.dto;

import com.moodmate.domain.emotion.Emotion;
import io.swagger.v3.oas.annotations.media.Schema;

public record EmotionResponse (
        @Schema(description = "감정 id", example = "1")
        Long id,

        @Schema(description = "감정 이름", example = "기쁨")
        String name
) {
        public EmotionResponse(Emotion emotion) {
                this(emotion.getId(), emotion.getName());
        }
}
