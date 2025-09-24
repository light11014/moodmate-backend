package com.moodmate.domain.feedback.repository;

import com.moodmate.domain.feedback.entity.AiFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AiFeedbackRepository extends JpaRepository<AiFeedback, Long> {
    Optional<AiFeedback> findByDiaryId(Long diaryId);

    @Query("SELECT af FROM AiFeedback af JOIN af.diary d WHERE af.user.id = :userId AND d.date BETWEEN :startDate AND :endDate ORDER BY d.date DESC")
    List<AiFeedback> findByUserIdAndDateRange(@Param("userId") Long userId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);
}
