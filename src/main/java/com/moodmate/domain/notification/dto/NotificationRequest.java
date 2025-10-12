package com.moodmate.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record NotificationRequest(
        @NotBlank
        @Schema(description = "알림 내용", example = "일기 작성 시간입니다!")
        String content
) {}