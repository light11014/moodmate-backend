package com.moodmate.domain.alarm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AlarmRequest(
        @NotBlank
        @Schema(description = "알람 내용", example = "일기 작성 시간입니다!")
        String content
) {}