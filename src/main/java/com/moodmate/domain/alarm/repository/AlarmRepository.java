package com.moodmate.domain.alarm.repository;

import com.moodmate.domain.alarm.entity.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    // 특정 사용자의 모든 알람 조회 (최신순)
    @Query("SELECT a FROM Alarm a WHERE a.user.id = :userId ORDER BY a.created_at DESC")
    List<Alarm> findUncheckedByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    // 특정 사용자의 확인하지 않은 알람 조회
    @Query("SELECT a FROM Alarm a WHERE a.user.id = :userId AND a.isChecked = true ORDER BY a.created_at DESC")
    List<Alarm> findCheckedByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    // 특정 사용자의 확인한 알람 조회
    @Query("SELECT a FROM Alarm a WHERE a.user.id = :userId AND a.isChecked = false ORDER BY a.created_at DESC")
    List<Alarm> findByUserIdAndIsCheckedFalseOrderByCreatedAtDesc(Long userId);

    // 확인하지 않은 알람 개수
    long countByUserIdAndIsCheckedFalse(Long userId);
}