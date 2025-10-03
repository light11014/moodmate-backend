package com.moodmate.domain.feedback.dto;

import com.moodmate.domain.feedback.entity.FeedbackStyle;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 피드백 생성 시 스타일만 받는 요청 DTO
 diaryId는 URL 경로에서 받으므로 제외*/
public record FeedbackStyleRequest(
        @NotNull
        @Schema(description = "피드백 스타일", example = "COMFORT")
        FeedbackStyle feedbackStyle
) {}