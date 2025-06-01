package com.moodmate.domain.diary.dto;

import com.moodmate.domain.diary.entity.Diary;
import lombok.Getter;

@Getter
public class DiaryResponseDto {
    private final String content;

    public DiaryResponseDto(Diary diary) {
        this.content = diary.getContent();
    }
}
