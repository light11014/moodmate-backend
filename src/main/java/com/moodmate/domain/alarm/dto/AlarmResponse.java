package com.moodmate.domain.alarm.dto;

import com.moodmate.domain.alarm.entity.Alarm;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record AlarmResponse(
        @Schema(description = "알람 ID", example = "1")
        Long id,

        @Schema(description = "알람 내용", example = "일기 작성 시간입니다!")
        String content,

        @Schema(description = "확인 여부", example = "false")
        boolean isChecked,

        @Schema(description = "알람 생성 시간")
        LocalDateTime createdAt
) {
    public AlarmResponse(Alarm alarm) {
        this(
                alarm.getId(),
                alarm.getContent(),
                alarm.isChecked(),
                alarm.getCreated_at()
        );
    }
}