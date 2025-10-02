package com.moodmate.domain.diary.dto;

import com.moodmate.domain.diary.entity.Diary;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record DiaryResponse(
        @Schema(description = "일기 ID", example = "123")
        Long diaryId,

        @Schema(description = "일기 내용", example = "오늘은 날씨가 좋았다.")
        String content,

        @Schema(description = "일기 날짜", example = "2025-04-08")
        LocalDate date,

        @Schema(description = "감정 목록")
        List<EmotionDto> emotions,

        @Schema(description = "일기 작성 시간")
        LocalDateTime createdAt,

        @Schema(description = "일기 수정 시간")
        LocalDateTime updatedAt
) {
    public DiaryResponse(Diary diary) {
        this(
                diary.getId(),
                diary.getContent(),
                diary.getDate(),
                diary.getDiaryEmotions().stream()
                        .map(e -> new EmotionDto(e.getEmotion().getName(), e.getIntensity()))
                        .toList(),
                diary.getCreated_at(),
                diary.getUpdated_at()
        );
    }
}