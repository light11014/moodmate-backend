package com.moodmate.domain.tracking.dayOfWeek;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "요일별 감정 조회 응답")
public record DayOfWeekEmotionResponse(
        @Schema(description = "요일별 감정 조회 메타데이터")
        WeekMeta meta,

        @Schema(description = "요일별 감정 데이터",
                example = ""
        )
        List<WeekDto> data
) {
}
