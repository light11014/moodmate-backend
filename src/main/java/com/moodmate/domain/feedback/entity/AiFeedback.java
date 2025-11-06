package com.moodmate.domain.feedback.entity;

import com.moodmate.common.BaseTimeEntity;
import com.moodmate.domain.diary.entity.Diary;
import com.moodmate.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// AiFeedback.java
@Entity
@Getter
@NoArgsConstructor
@Table(name = "ai_feedback")
public class AiFeedback extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_ai_feedback_user",
                    foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
            ))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id", nullable = false)
    private Diary diary;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String response;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_style", nullable = false)
    private FeedbackStyle feedbackStyle;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Builder
    public AiFeedback(User user, Diary diary, String summary, String response, FeedbackStyle feedbackStyle, LocalDateTime requestedAt) {
        this.user = user;
        this.diary = diary;
        this.summary = summary;
        this.response = response;
        this.feedbackStyle = feedbackStyle;
        this.requestedAt = requestedAt != null ? requestedAt : LocalDateTime.now();
    }
}