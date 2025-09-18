package com.moodmate.domain.tracking.dto.ratio;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "감정 비율 조회 응답")
public record EmotionRatioResponse(
        @Schema(description = "메타 데이터")
        RatioMeta meta,

        @Schema(description = "감정 비율 데이터")
        List<EmotionRatioDto> data
) {

}
