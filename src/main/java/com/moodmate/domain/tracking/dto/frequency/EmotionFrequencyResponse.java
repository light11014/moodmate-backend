package com.moodmate.domain.tracking.dto.frequency;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record EmotionFrequencyResponse(
        @Schema(description = "메타 데이터")
        FrequencyMeta meta,

        @Schema(description = "감정 빈도 데이터")
        List<EmotionFrequencyDto> data
) {}
