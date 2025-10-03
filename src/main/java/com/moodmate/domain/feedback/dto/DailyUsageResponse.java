package com.moodmate.domain.feedback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DailyUsageResponse {
    @Schema(description = "오늘 사용한 피드백 횟수", example = "1")
    private int usedCount;

    @Schema(description = "일일 최대 허용 횟수", example = "2")
    private int maxCount;

    @Schema(description = "남은 사용 가능 횟수", example = "1")
    private int remainingCount;
}
