package com.moodmate.domain.tracking.word.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "단어 빈도 분석 응답")
public record WordFrequencyResponse(
        @Schema(description = "메타데이터")
        WordFrequencyMeta meta,

        @Schema(description = "단어 빈도 목록")
        List<WordFrequency> data
) {
}
