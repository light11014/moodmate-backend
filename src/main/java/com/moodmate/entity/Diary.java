package com.moodmate.entity;

import com.moodmate.util.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Diary extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiaryEmotion> diaryEmotions = new ArrayList<>();

    public void addDiaryEmotion(DiaryEmotion diaryEmotion) {
        diaryEmotions.add(diaryEmotion);
        diaryEmotion.setDiary(this);
    }

    public Diary(String content, LocalDate date, User user) {
        this.content = content;
        this.date = date;
        this.user = user;
    }
}

