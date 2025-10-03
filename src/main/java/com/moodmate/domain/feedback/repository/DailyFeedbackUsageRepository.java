package com.moodmate.domain.feedback.repository;

import com.moodmate.domain.feedback.entity.DailyFeedbackUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import com.moodmate.domain.feedback.entity.AiFeedback;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface DailyFeedbackUsageRepository extends JpaRepository<DailyFeedbackUsage, Long> {
    Optional<DailyFeedbackUsage> findByUserIdAndUsageDate(Long userId, LocalDate usageDate);
}


