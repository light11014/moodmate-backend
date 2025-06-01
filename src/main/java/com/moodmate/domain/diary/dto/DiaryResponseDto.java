package com.moodmate.domain.diary.dto;

import com.moodmate.domain.diary.entity.Diary;
import com.moodmate.domain.diary.entity.DiaryEmotion;
import lombok.Getter;

import java.util.List;

@Getter
public class DiaryResponseDto {
    private final String content;
    private final List<EmotionDto> emotions;

    public DiaryResponseDto(Diary diary) {
        this.content = diary.getContent();
        this.emotions = diary.getDiaryEmotions().stream()
                .map(e -> new EmotionDto(e.getEmotion().getName(), e.getIntensity()))
                .toList();
    }

    @Getter
    public static class EmotionDto {
        private final String name;
        private final int intensity;

        public EmotionDto(String name, int intensity) {
            this.name = name;
            this.intensity = intensity;
        }
    }
}
