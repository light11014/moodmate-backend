package com.moodmate.domain.tracking.dto;

import com.moodmate.domain.diary.dto.EmotionDto;
import io.swagger.v3.oas.annotations.media.Schema;

public record EmotionFrequency(
        @Schema(description = "감정 이름", example = "기쁨")
        String name,

        @Schema(description = "기간별 감정 횟수", example = "10")
        Long count
) {
}
