package com.moodmate.domain.diary.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class DiaryRequestDto {
    @NotBlank
    private String content;

    @NotNull
    private LocalDate date;

    @NotEmpty
    private List<EmotionRequest> emotions;

    @Getter
    @Setter
    public static class EmotionRequest {
        @NotBlank
        private String name;

        @Min(1)
        @Max(5)
        private int intensity;
    }
}
