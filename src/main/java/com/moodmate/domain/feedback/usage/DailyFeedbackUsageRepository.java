package com.moodmate.domain.feedback.usage;

import com.moodmate.domain.feedback.usage.DailyFeedbackUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;


public interface DailyFeedbackUsageRepository extends JpaRepository<DailyFeedbackUsage, Long> {
    Optional<DailyFeedbackUsage> findByUserIdAndUsageDate(Long userId, LocalDate usageDate);
}


