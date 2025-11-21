package com.moodmate.domain.feedback.entity;

import com.moodmate.common.BaseTimeEntity;
import com.moodmate.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 기간별 종합 피드백 엔티티
 * 사용자가 설정한 기간의 일기들을 종합 분석한 결과를 저장
 */
@Entity
@Getter
@NoArgsConstructor
@Table(name = "period_analysis",
        indexes = {
                @Index(name = "idx_period_analysis_user_dates",
                        columnList = "user_id, start_date, end_date")
        })
public class PeriodAnalysis extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_period_analysis_user",
                    foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
            ))
    private User user;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "analyzed_diary_count", nullable = false)
    private int analyzedDiaryCount;

    @Column(name = "period_summary", nullable = false, columnDefinition = "TEXT")
    private String periodSummary;

    @Column(name = "recommendations", nullable = false, columnDefinition = "TEXT")
    private String recommendations;

    @Column(name = "combined_summaries", columnDefinition = "TEXT")
    private String combinedSummaries; // 분석에 사용된 일기 요약들 (선택적)

    @Builder
    public PeriodAnalysis(User user, LocalDate startDate, LocalDate endDate,
                          int analyzedDiaryCount, String periodSummary,
                          String recommendations, String combinedSummaries) {
        this.user = user;
        this.startDate = startDate;
        this.endDate = endDate;
        this.analyzedDiaryCount = analyzedDiaryCount;
        this.periodSummary = periodSummary;
        this.recommendations = recommendations;
        this.combinedSummaries = combinedSummaries;
    }
}