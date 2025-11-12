package com.moodmate.domain.feedback.entity;

import com.moodmate.common.BaseTimeEntity;
import com.moodmate.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "feedback_processing_lock",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id"})
        })
public class FeedbackProcessingLock extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_feedback_processing_lock_user",
                    foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
            ))
    private User user;

    @Column(name = "diary_id", nullable = false)
    private Long diaryId;

    @Column(name = "locked_at", nullable = false)
    private LocalDateTime lockedAt;

    @Column(name = "lock_key", nullable = false, unique = true)
    private String lockKey;

    @Builder
    public FeedbackProcessingLock(User user, Long diaryId, String lockKey) {
        this.user = user;
        this.diaryId = diaryId;
        this.lockKey = lockKey;
        this.lockedAt = LocalDateTime.now();
    }
}