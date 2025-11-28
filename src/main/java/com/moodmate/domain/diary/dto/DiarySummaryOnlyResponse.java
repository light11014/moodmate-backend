package com.moodmate.domain.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record DiarySummaryOnlyResponse(
        @Schema(description = "일기 날짜", example = "2025-04-08")
        LocalDate date,

        @Schema(description = "일기 ID", example = "123")
        Long diaryId,

        @Schema(description = "AI 피드백 요약 (피드백이 없으면 빈 문자열)",
                example = "오늘 하루 • 긍정적인 변화")
        String summary
) {}