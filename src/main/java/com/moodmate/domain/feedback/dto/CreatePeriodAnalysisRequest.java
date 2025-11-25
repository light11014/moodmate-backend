package com.moodmate.domain.feedback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 종합 피드백 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePeriodAnalysisRequest {
    @Schema(description = "분석 시작 날짜", example = "2025-09-01")
    private LocalDate startDate;

    @Schema(description = "분석 종료 날짜", example = "2025-09-30")
    private LocalDate endDate;

    @Schema(description = "기존 분석 덮어쓰기 여부", example = "false")
    private boolean overwrite = false;

    public void validate() {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("시작 날짜와 종료 날짜는 필수입니다.");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 이전이어야 합니다.");
        }
    }
}