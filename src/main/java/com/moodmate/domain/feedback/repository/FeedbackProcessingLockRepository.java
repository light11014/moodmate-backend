package com.moodmate.domain.feedback.repository;

import com.moodmate.domain.feedback.entity.FeedbackProcessingLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface FeedbackProcessingLockRepository extends JpaRepository<FeedbackProcessingLock, Long> {

    Optional<FeedbackProcessingLock> findByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM FeedbackProcessingLock fpl WHERE fpl.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM FeedbackProcessingLock fpl WHERE fpl.lockKey = :lockKey")
    void deleteByLockKey(@Param("lockKey") String lockKey);

    // 오래된 락 제거 (타임아웃 처리)
    @Modifying
    @Query("DELETE FROM FeedbackProcessingLock fpl WHERE fpl.lockedAt < :timeoutTime")
    int deleteExpiredLocks(@Param("timeoutTime") LocalDateTime timeoutTime);
}