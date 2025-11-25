package com.moodmate.domain.feedback.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodAnalysisListItem {
    @Schema(description = "분석 ID")
    private Long analysisId;

    @Schema(description = "분석 시작 날짜")
    private LocalDate startDate;

    @Schema(description = "분석 종료 날짜")
    private LocalDate endDate;

    @Schema(description = "분석된 일기 개수")
    private int analyzedDiaryCount;

    @Schema(description = "분석 생성 시간")
    private LocalDateTime createdAt;
}
