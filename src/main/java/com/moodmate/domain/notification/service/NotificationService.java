package com.moodmate.domain.notification.service;

import com.moodmate.domain.notification.dto.NotificationListResponse;
import com.moodmate.domain.notification.dto.NotificationRequest;
import com.moodmate.domain.notification.dto.NotificationResponse;
import com.moodmate.domain.notification.entity.Notification;
import com.moodmate.domain.notification.repository.NotificationRepository;
import com.moodmate.domain.user.UserRepository;
import com.moodmate.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * 알람 저장
     */
    public NotificationResponse saveNotification(Long userId, NotificationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Notification notification = Notification.builder()
                .user(user)
                .content(request.content())
                .build();

        notificationRepository.save(notification);
        log.info("알림 저장 완료 - 사용자: {}, 알림 ID: {}", userId, notification.getId());

        return new NotificationResponse(notification);
    }

    /**
     * 알림 조회 - 모든 알림
     */
    @Transactional(readOnly = true)
    public NotificationListResponse getNotifications(Long userId, Boolean isChecked) {
        List<Notification> notis;

        if (isChecked == null) {
            // 전체 조회
            notis = notificationRepository.findUncheckedByUserIdOrderByCreatedAtDesc(userId);
        } else if (isChecked) {
            // 확인한 알람만
            notis = notificationRepository.findCheckedByUserIdOrderByCreatedAtDesc(userId);
        } else {
            // 확인하지 않은 알람만
            notis = notificationRepository.findByUserIdAndIsCheckedFalseOrderByCreatedAtDesc(userId);
        }

        List<NotificationResponse> responses = notis.stream()
                .map(NotificationResponse::new)
                .toList();

        long uncheckedCount = notificationRepository.countByUserIdAndIsCheckedFalse(userId);

        return new NotificationListResponse(responses, uncheckedCount);
    }

    /**
     * 특정 알람 조회
     */
    @Transactional(readOnly = true)
    public NotificationResponse getNotification(Long userId, Long notificationId) throws AccessDeniedException {
        Notification noti = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));

        if (!Objects.equals(noti.getUser().getId(), userId)) {
            throw new AccessDeniedException("해당 알림에 대한 권한이 없습니다.");
        }

        return new NotificationResponse(noti);
    }

    /**
     * 알람 확인 상태 변경
     */
    public NotificationResponse updateNotificationCheckedStatus(Long userId, Long notificationId, boolean isChecked) throws AccessDeniedException {
        Notification noti = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));

        if (!Objects.equals(noti.getUser().getId(), userId)) {
            throw new AccessDeniedException("해당 알림에 대한 권한이 없습니다.");
        }

        if (isChecked) {
            noti.markAsChecked();
        } else {
            noti.markAsUnchecked();
        }

        notificationRepository.save(noti);
        log.info("알림 확인 상태 변경 - 알림 ID: {}, 확인 여부: {}", notificationId, isChecked);

        return new NotificationResponse(noti);
    }

    /**
     * 알람 삭제
     */
    public void deleteNotification(Long userId, Long notificationId) throws AccessDeniedException {
        Notification noti = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));

        if (!Objects.equals(noti.getUser().getId(), userId)) {
            throw new AccessDeniedException("해당 알림에 대한 권한이 없습니다.");
        }

        notificationRepository.delete(noti);
        log.info("알림 삭제 완료 - 사용자: {}, 알람 ID: {}", userId, notificationId);
    }

    /**
     * 확인하지 않은 알림 개수 조회
     */
    @Transactional(readOnly = true)
    public long getUncheckedNotificationCount(Long userId) {
        return notificationRepository.countByUserIdAndIsCheckedFalse(userId);
    }
}