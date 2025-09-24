// FeedbackRequest.java
package com.moodmate.domain.feedback.dto;

import com.moodmate.domain.feedback.entity.FeedbackStyle;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

import com.moodmate.domain.feedback.entity.AiFeedback;


public record FeedbackRequest(
        @NotNull
        @Schema(description = "일기 ID", example = "1")
        Long diaryId,

        @NotNull
        @Schema(description = "피드백 스타일", example = "COMFORT")
        FeedbackStyle feedbackStyle
) {}


