package com.moodmate.domain.diary.entity;

import com.moodmate.domain.emotion.Emotion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "diary_emotion")
public class DiaryEmotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Diary diary;

    @ManyToOne(fetch = FetchType.LAZY)
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
