package com.moodmate.domain.tracking.word.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "단어 빈도 응답")
public record WordFrequency(
        @Schema(description = "단어", example = "행복")
        String word,

        @Schema(description = "출현 횟수", example = "15")
        Long count) {
}
