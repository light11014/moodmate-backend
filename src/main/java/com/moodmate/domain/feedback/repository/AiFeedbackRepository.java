package com.moodmate.domain.feedback.repository;

import com.moodmate.domain.feedback.entity.AiFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AiFeedbackRepository extends JpaRepository<AiFeedback, Long> {
    // 특정 일기의 가장 최근 피드백 조회 (하나만 필요한 경우)
    @Query("SELECT af FROM AiFeedback af WHERE af.diary.id = :diaryId ORDER BY af.requestedAt DESC LIMIT 1")
    Optional<AiFeedback> findLatestByDiaryId(@Param("diaryId") Long diaryId);

    // 특정 일기의 모든 피드백 조회 (여러 개 가능)
    @Query("SELECT af FROM AiFeedback af WHERE af.diary.id = :diaryId ORDER BY af.requestedAt DESC")
    List<AiFeedback> findAllByDiaryId(@Param("diaryId") Long diaryId);

    @Query("SELECT af FROM AiFeedback af JOIN af.diary d WHERE af.user.id = :userId AND d.date BETWEEN :startDate AND :endDate ORDER BY d.date DESC, af.requestedAt DESC")
    List<AiFeedback> findByUserIdAndDateRange(@Param("userId") Long userId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);
}