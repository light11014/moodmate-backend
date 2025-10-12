package com.moodmate.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record NotificationListResponse(
        @Schema(description = "총 알림 개수")
        int totalCount,

        @Schema(description = "확인하지 않은 알림 개수")
        long uncheckedCount,

        @Schema(description = "알림 목록")
        List<NotificationResponse> notifications
) {
    public NotificationListResponse(List<NotificationResponse> notifications, long uncheckedCount) {
        this(notifications.size(), uncheckedCount, notifications);
    }
}