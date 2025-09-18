package com.moodmate.domain.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class DiaryRequest {
    @NotBlank
    @Schema(description = "일기 내용", example = "오늘은 날씨가 좋았다.")
    private String content;

    @NotNull
    @Schema(description = "일기 날짜", example = "2025-04-08")
    private LocalDate date;

    @NotEmpty
    private List<EmotionDto> emotions;
}
