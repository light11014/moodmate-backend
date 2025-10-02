package com.moodmate.domain.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiaryPeriodRequest {
    @Schema(description = "조회 시작 날짜", example = "2025-07-01")
    private LocalDate startDate;

    @Schema(description = "조회 종료 날짜", example = "2025-07-05")
    private LocalDate endDate;

    @Schema(description = "응답에 포함할 내용 (summary: 요약만, full: 전체 내용)", example = "summary", allowableValues = {"summary", "full"})
    private String require;

    public void validate() {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 이전이어야 합니다.");
        }

        if (require != null && !require.equals("summary") && !require.equals("full")) {
            throw new IllegalArgumentException("require는 'summary' 또는 'full'만 가능합니다.");
        }
    }

    public boolean isSummaryOnly() {
        return "summary".equals(require);
    }

    public boolean isFullContent() {
        return "full".equals(require);
    }
}