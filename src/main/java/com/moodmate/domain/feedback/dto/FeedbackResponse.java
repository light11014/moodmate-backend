package com.moodmate.domain.feedback.dto;

import com.moodmate.domain.feedback.entity.AiFeedback;
import com.moodmate.domain.feedback.entity.FeedbackStyle;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record FeedbackResponse(
        @Schema(description = "피드백 ID", example = "1")
        Long feedbackId,

        @Schema(description = "일기 요약", example = "오늘 좋은 하루를 보내셨군요.")
        String summary,

        @Schema(description = "AI 피드백 응답", example = "정말 멋진 하루를 보내셨네요! 이런 긍정적인 마음가짐이 계속 이어지길 응원합니다.")
        String response,

        @Schema(description = "피드백 스타일", example = "PRAISE")
        FeedbackStyle feedbackStyle,

        @Schema(description = "피드백 요청 시간")
        LocalDateTime requestedAt,

        @Schema(description = "피드백 저장 시간")
        LocalDateTime createdAt
) {
    public FeedbackResponse(AiFeedback feedback) {
        this(
                feedback.getId(),
                feedback.getSummary(),
                feedback.getResponse(),
                feedback.getFeedbackStyle(),
                feedback.getRequestedAt(),
                feedback.getCreated_at()
        );
    }
}