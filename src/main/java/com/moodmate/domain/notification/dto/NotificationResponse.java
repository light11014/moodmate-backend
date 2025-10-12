package com.moodmate.domain.notification.dto;

import com.moodmate.domain.notification.entity.Notification;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record NotificationResponse(
        @Schema(description = "알림 ID", example = "1")
        Long id,

        @Schema(description = "알림 내용", example = "일기 작성 시간입니다!")
        String content,

        @Schema(description = "확인 여부", example = "false")
        boolean isChecked,

        @Schema(description = "알림 생성 시간")
        LocalDateTime createdAt
) {
    public NotificationResponse(Notification notification) {
        this(
                notification.getId(),
                notification.getContent(),
                notification.isChecked(),
                notification.getCreated_at()
        );
    }
}