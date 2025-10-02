package com.moodmate.domain.tracking.dayOfWeek;

import com.moodmate.domain.emotion.Emotion;

public interface DayOfWeekEmotionProjection {
    Integer getDayOfWeek(); // 0=일요일, 1=월요일, ..., 6=토요일
    Long getEmotionId();
    String getEmotionName();
    Long getCount();
    Double getAvgIntensity();
    Long getDiaryCount();
}