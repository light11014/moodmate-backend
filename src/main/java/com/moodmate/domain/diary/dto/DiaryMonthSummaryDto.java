package com.moodmate.domain.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class DiaryMonthSummaryDto {
    private LocalDate date;
    private List<EmotionDto> emotions;

    @Getter
    @AllArgsConstructor
    public static class EmotionDto {
        private String name;
        private int intensity;
    }
}
