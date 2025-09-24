package com.moodmate.domain.feedback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class FeedbackHistoryResponse {
    @Schema(description = "조회 시작일")
    private LocalDate startDate;

    @Schema(description = "조회 종료일")
    private LocalDate endDate;

    @Schema(description = "총 피드백 개수")
    private int totalCount;

    @Schema(description = "피드백 목록 (summary와 response 모두 포함)")
    private List<FeedbackHistoryItem> feedbacks;

    public FeedbackHistoryResponse(LocalDate startDate, LocalDate endDate, List<FeedbackHistoryItem> feedbacks) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalCount = feedbacks.size();
        this.feedbacks = feedbacks;
    }
}