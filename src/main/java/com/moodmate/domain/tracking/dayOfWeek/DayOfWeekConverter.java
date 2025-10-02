package com.moodmate.domain.tracking.dayOfWeek;

import java.time.DayOfWeek;
import java.util.Arrays;

public enum DayOfWeekConverter {
    SUNDAY(DayOfWeek.SUNDAY, "일요일"),
    MONDAY(DayOfWeek.MONDAY, "월요일"),
    TUESDAY(DayOfWeek.TUESDAY, "화요일"),
    WEDNESDAY(DayOfWeek.WEDNESDAY, "수요일"),
    THURSDAY(DayOfWeek.THURSDAY, "목요일"),
    FRIDAY(DayOfWeek.FRIDAY, "금요일"),
    SATURDAY(DayOfWeek.SATURDAY, "토요일");

    private final DayOfWeek dayOfWeek;
    private final String koreanName;

    DayOfWeekConverter(DayOfWeek dayOfWeek, String koreanName) {
        this.dayOfWeek = dayOfWeek;
        this.koreanName = koreanName;
    }

    public static DayOfWeek fromPostgresValue(int value) {
        return values()[value].dayOfWeek;
    }

    public static String getKoreanName(DayOfWeek dayOfWeek) {
        return values()[dayOfWeek.getValue() % 7].koreanName;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public String getKoreanName() {
        return koreanName;
    }
}