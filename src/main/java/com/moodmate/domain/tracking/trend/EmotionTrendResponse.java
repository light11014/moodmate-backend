package com.moodmate.domain.tracking.trend;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record EmotionTrendResponse(
        @Schema(description = "메타 데이터")
        TimeLineMeta meta,

        @Schema(description = "타임 라인 데이터")
        List<TimeLineDto> data){}
