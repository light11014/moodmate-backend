package com.moodmate.domain.diary.dto;

import com.moodmate.domain.diary.entity.Diary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
public class DiaryResponse {
    @Schema(description = "일기 내용", example = "오늘은 날씨가 좋았다.")
    private final String content;

    private final List<EmotionDto> emotions;

    public DiaryResponse(Diary diary) {
        this.content = diary.getContent();
        this.emotions = diary.getDiaryEmotions().stream()
                .map(e -> new EmotionDto(e.getEmotion().getName(), e.getIntensity()))
                .toList();
    }
}
