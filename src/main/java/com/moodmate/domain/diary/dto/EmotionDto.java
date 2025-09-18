package com.moodmate.domain.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

public record EmotionDto(
        @NotBlank
        @Schema(description = "감정 이름", example = "기쁨")
        String name,

        @Min(1)
        @Max(5)
        @Schema(description = "감정 강도 (1~5)", example = "4")
        int intensity
) {}