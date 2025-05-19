package com.moodmate.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
public class DiaryEmotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Diary diary;

    @ManyToOne
    private Emotion emotion;

    private int intensity;

    public void setDiary(Diary diary) {
        this.diary = diary;
    }

    public DiaryEmotion(Emotion emotion, int intensity) {
        this.emotion = emotion;
        this.intensity = intensity;
    }
}
