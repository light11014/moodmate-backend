package com.moodmate.domain.tracking.word.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "단어 빈도 분석 메타데이터")
public record WordFrequencyMeta(
        @Schema(description = "사용자 ID")
        Long userId,

        @Schema(description = "조회 시작일", example = "2025-01-01")
        LocalDate startDate,

        @Schema(description = "조회 종료일", example = "2025-12-31")
        LocalDate endDate,

        @Schema(description = "분석된 일기 개수", example = "50")
        Long diaryCount,

        @Schema(description = "총 단어 수", example = "1500")
        Long totalWordCount,

        @Schema(description = "고유 단어 수", example = "350")
        Integer uniqueWordCount,

        @Schema(description = "생성 시간")
        LocalDateTime generatedAt
) {
}
