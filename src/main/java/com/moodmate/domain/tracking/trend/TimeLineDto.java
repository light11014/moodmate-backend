package com.moodmate.domain.tracking.trend;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record TimeLineDto(
        @Schema(description = "감정 이름", example = "기쁨")
        String emotion,

        @Schema(
                description = "감정 타임 라인",
                example = "[{\"day\":\"2025-09-01\",\"intensity\":5},{\"day\":\"2025-09-15\",\"intensity\":3}]"
        )
        List<TimeLinePointDto> timeline
) {
}
