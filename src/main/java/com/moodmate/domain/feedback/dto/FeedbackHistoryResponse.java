package com.moodmate.domain.feedback.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record FeedbackHistoryResponse(
        @Schema(description = "조회 시작일")
        LocalDate startDate,

        @Schema(description = "조회 종료일")
        LocalDate endDate,

        @Schema(description = "총 피드백 개수")
        int totalCount,

        @Schema(description = "피드백 목록 (summary와 response 모두 포함)")
        List<FeedbackResponse> feedbacks
) {
    public FeedbackHistoryResponse(LocalDate startDate, LocalDate endDate, List<FeedbackResponse> feedbacks) {
        this(startDate, endDate, feedbacks.size(), feedbacks);
    }
}