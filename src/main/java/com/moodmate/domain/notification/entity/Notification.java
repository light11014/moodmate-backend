package com.moodmate.domain.notification.entity;

import com.moodmate.common.BaseTimeEntity;
import com.moodmate.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "Notification")
public class Notification extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private boolean isChecked = false;

    @Builder
    public Notification(User user, String content) {
        this.user = user;
        this.content = content;
        this.isChecked = false;
    }

    public void markAsChecked() {
        this.isChecked = true;
    }

    public void markAsUnchecked() {
        this.isChecked = false;
    }
}