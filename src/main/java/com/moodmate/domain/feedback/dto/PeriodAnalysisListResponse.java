package com.moodmate.domain.feedback.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PeriodAnalysisListResponse {
    @Schema(description = "총 분석 개수")
    private int totalCount;

    @Schema(description = "종합 피드백 목록")
    private List<PeriodAnalysisListItem> analyses;

    public PeriodAnalysisListResponse(List<PeriodAnalysisListItem> analyses) {
        this.totalCount = analyses.size();
        this.analyses = analyses;
    }
}
