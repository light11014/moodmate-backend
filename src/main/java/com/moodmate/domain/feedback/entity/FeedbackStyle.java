// FeedbackStyle.java
package com.moodmate.domain.feedback.entity;

import com.moodmate.common.BaseTimeEntity;
import com.moodmate.domain.diary.entity.Diary;
import com.moodmate.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


public enum FeedbackStyle {
    COMFORT("위로"),
    PRAISE("칭찬"),
    DIRECT("직설적"),
    ENCOURAGING("격려");

    private final String description;

    FeedbackStyle(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}


