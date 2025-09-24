package com.moodmate.domain.feedback.dto;

import com.moodmate.domain.feedback.entity.AiFeedback;
import com.moodmate.domain.feedback.entity.FeedbackStyle;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class FeedbackResponse {
    @Schema(description = "피드백 ID", example = "1")
    private final Long feedbackId;

    @Schema(description = "일기 요약", example = "오늘 좋은 하루를 보내셨군요.")
    private final String summary;

    @Schema(description = "AI 피드백 응답", example = "정말 멋진 하루를 보내셨네요! 이런 긍정적인 마음가짐이 계속 이어지길 응원합니다.")
    private final String response;

    @Schema(description = "피드백 스타일", example = "PRAISE")
    private final FeedbackStyle feedbackStyle;

    @Schema(description = "피드백 생성 시간")
    private final LocalDateTime createdAt;

    public FeedbackResponse(AiFeedback feedback) {
        this.feedbackId = feedback.getId();
        this.summary = feedback.getSummary();
        this.response = feedback.getResponse();
        this.feedbackStyle = feedback.getFeedbackStyle();
        this.createdAt = feedback.getCreated_at();
    }
}
