package com.moodmate.domain.notification.repository;

import com.moodmate.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // 특정 사용자의 모든 알림 조회 (최신순)
    @Query("SELECT a FROM Notification a WHERE a.user.id = :userId ORDER BY a.created_at DESC")
    List<Notification> findUncheckedByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    // 특정 사용자의 확인하지 않은 알림 조회
    @Query("SELECT a FROM Notification a WHERE a.user.id = :userId AND a.isChecked = true ORDER BY a.created_at DESC")
    List<Notification> findCheckedByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    // 특정 사용자의 확인한 알림 조회
    @Query("SELECT a FROM Notification a WHERE a.user.id = :userId AND a.isChecked = false ORDER BY a.created_at DESC")
    List<Notification> findByUserIdAndIsCheckedFalseOrderByCreatedAtDesc(Long userId);

    // 확인하지 않은 알림 개수
    long countByUserIdAndIsCheckedFalse(Long userId);
}