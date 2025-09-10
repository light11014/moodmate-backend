package com.moodmate.domain.tracking.dto;

import com.moodmate.domain.emotion.Emotion;

public record EmotionFrequency(
        Emotion emotion,
        Long count
) {
}
