package com.moodmate.domain.feedback.repository;

import com.moodmate.domain.feedback.entity.PeriodAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PeriodAnalysisRepository extends JpaRepository<PeriodAnalysis, Long> {

    /**
     * 특정 사용자의 모든 종합 분석 조회 (최신순)
     */
    @Query("SELECT pa FROM PeriodAnalysis pa WHERE pa.user.id = :userId ORDER BY pa.created_at DESC")
    List<PeriodAnalysis> findAllByUserId(@Param("userId") Long userId);

    /**
     * 특정 기간의 종합 분석 조회
     */
    @Query("SELECT pa FROM PeriodAnalysis pa WHERE pa.user.id = :userId " +
            "AND pa.startDate = :startDate AND pa.endDate = :endDate " +
            "ORDER BY pa.created_at DESC LIMIT 1")
    Optional<PeriodAnalysis> findLatestByUserIdAndPeriod(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 특정 날짜 범위와 겹치는 분석들 조회
     */
    @Query("SELECT pa FROM PeriodAnalysis pa WHERE pa.user.id = :userId " +
            "AND ((pa.startDate <= :endDate AND pa.endDate >= :startDate)) " +
            "ORDER BY pa.startDate DESC")
    List<PeriodAnalysis> findOverlappingAnalyses(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 특정 ID로 사용자의 분석 조회 (권한 확인용)
     */
    @Query("SELECT pa FROM PeriodAnalysis pa WHERE pa.id = :analysisId AND pa.user.id = :userId")
    Optional<PeriodAnalysis> findByIdAndUserId(
            @Param("analysisId") Long analysisId,
            @Param("userId") Long userId
    );
}