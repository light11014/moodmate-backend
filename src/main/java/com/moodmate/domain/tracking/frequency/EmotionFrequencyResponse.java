package com.moodmate.domain.tracking.frequency;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record EmotionFrequencyResponse(
        @Schema(description = "메타 데이터")
        FrequencyMeta meta,

        @Schema(
                description = "감정 빈도 데이터",
                example = "[{\"emotion\":\"기쁨\",\"count\":2},{\"emotion\":\"슬픔\",\"count\":1}]"
        )
        List<FrequencyDto> data
) {}
