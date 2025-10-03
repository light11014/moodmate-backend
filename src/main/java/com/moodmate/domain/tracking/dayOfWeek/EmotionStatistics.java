package com.moodmate.domain.tracking.dayOfWeek;

import com.moodmate.domain.emotion.Emotion;
import io.swagger.v3.oas.annotations.media.Schema;

public record EmotionStatistics(
        @Schema(description = "감정 종류", example = "기쁨")
        String emotionType,

        @Schema(description = "해당 감정을 느낀 횟수", example = "5")
        Integer count,

        @Schema(description = "비율 (%)", example = "62.5")
        Double percentage,

        @Schema(description = "평균 강도 (1-5)", example = "4.2")
        Double averageIntensity
) {
}
