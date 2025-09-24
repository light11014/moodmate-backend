// PeriodAnalysisResponse.java
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
public class PeriodAnalysisResponse {
    @Schema(description = "분석 ID")
    private Long analysisId;

    @Schema(description = "분석 시작 날짜")
    private LocalDate startDate;

    @Schema(description = "분석 종료 날짜")
    private LocalDate endDate;

    @Schema(description = "분석된 일기 개수")
    private int analyzedDiaryCount;

    @Schema(description = "기간 전체 요약",
            example = "이 기간 동안 새로운 직장에서의 적응과 개인적 성장에 중점을 둔 시기였습니다. 초기의 불안감에서 점차 자신감을 찾아가는 과정이 잘 드러나 있습니다.")
    private String periodSummary;

    @Schema(description = "감정 패턴 분석",
            example = "전반적으로 긍정적인 감정의 증가 추세를 보였으며, 특히 성취감과 만족감이 자주 나타났습니다. 스트레스 상황에서도 회복력이 향상되었습니다.")
    private String emotionalPattern;

    @Schema(description = "성장과 변화 분석",
            example = "문제 해결 능력이 크게 향상되었고, 새로운 도전을 두려워하지 않는 적극적인 태도로 변화했습니다. 자기 성찰의 깊이도 깊어졌습니다.")
    private String growthPattern;

    @Schema(description = "향후 권장사항",
            example = "현재의 긍정적인 momentum을 유지하면서도 때로는 충분한 휴식을 취하는 것이 좋겠습니다. 정기적인 운동이나 취미 활동을 통해 스트레스 관리를 더욱 체계화해보세요.")
    private String recommendations;

    @Schema(description = "분석 생성 시간")
    private LocalDateTime createdAt;

    public static PeriodAnalysisResponse create(
            LocalDate startDate,
            LocalDate endDate,
            int diaryCount,
            String periodSummary,
            String emotionalPattern,
            String growthPattern,
            String recommendations) {

        return PeriodAnalysisResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .analyzedDiaryCount(diaryCount)
                .periodSummary(periodSummary)
                .emotionalPattern(emotionalPattern)
                .growthPattern(growthPattern)
                .recommendations(recommendations)
                .createdAt(LocalDateTime.now())
                .build();
    }
}