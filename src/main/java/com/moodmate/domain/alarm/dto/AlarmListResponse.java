package com.moodmate.domain.alarm.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record AlarmListResponse(
        @Schema(description = "총 알람 개수")
        int totalCount,

        @Schema(description = "확인하지 않은 알람 개수")
        long uncheckedCount,

        @Schema(description = "알람 목록")
        List<AlarmResponse> alarms
) {
    public AlarmListResponse(List<AlarmResponse> alarms, long uncheckedCount) {
        this(alarms.size(), uncheckedCount, alarms);
    }
}