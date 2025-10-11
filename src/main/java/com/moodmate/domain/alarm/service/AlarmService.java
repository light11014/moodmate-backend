package com.moodmate.domain.alarm.service;

import com.moodmate.domain.alarm.dto.AlarmListResponse;
import com.moodmate.domain.alarm.dto.AlarmRequest;
import com.moodmate.domain.alarm.dto.AlarmResponse;
import com.moodmate.domain.alarm.entity.Alarm;
import com.moodmate.domain.alarm.repository.AlarmRepository;
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
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final UserRepository userRepository;

    /**
     * 알람 저장
     */
    public AlarmResponse saveAlarm(Long userId, AlarmRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Alarm alarm = Alarm.builder()
                .user(user)
                .content(request.content())
                .build();

        alarmRepository.save(alarm);
        log.info("알람 저장 완료 - 사용자: {}, 알람 ID: {}", userId, alarm.getId());

        return new AlarmResponse(alarm);
    }

    /**
     * 알람 조회 - 모든 알람
     */
    @Transactional(readOnly = true)
    public AlarmListResponse getAlarms(Long userId, Boolean isChecked) {
        List<Alarm> alarms;

        if (isChecked == null) {
            // 전체 조회
            alarms = alarmRepository.findUncheckedByUserIdOrderByCreatedAtDesc(userId);
        } else if (isChecked) {
            // 확인한 알람만
            alarms = alarmRepository.findCheckedByUserIdOrderByCreatedAtDesc(userId);
        } else {
            // 확인하지 않은 알람만
            alarms = alarmRepository.findByUserIdAndIsCheckedFalseOrderByCreatedAtDesc(userId);
        }

        List<AlarmResponse> responses = alarms.stream()
                .map(AlarmResponse::new)
                .toList();

        long uncheckedCount = alarmRepository.countByUserIdAndIsCheckedFalse(userId);

        return new AlarmListResponse(responses, uncheckedCount);
    }

    /**
     * 특정 알람 조회
     */
    @Transactional(readOnly = true)
    public AlarmResponse getAlarm(Long userId, Long alarmId) throws AccessDeniedException {
        Alarm alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new IllegalArgumentException("알람을 찾을 수 없습니다."));

        if (!Objects.equals(alarm.getUser().getId(), userId)) {
            throw new AccessDeniedException("해당 알람에 대한 권한이 없습니다.");
        }

        return new AlarmResponse(alarm);
    }

    /**
     * 알람 확인 상태 변경
     */
    public AlarmResponse updateAlarmCheckedStatus(Long userId, Long alarmId, boolean isChecked) throws AccessDeniedException {
        Alarm alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new IllegalArgumentException("알람을 찾을 수 없습니다."));

        if (!Objects.equals(alarm.getUser().getId(), userId)) {
            throw new AccessDeniedException("해당 알람에 대한 권한이 없습니다.");
        }

        if (isChecked) {
            alarm.markAsChecked();
        } else {
            alarm.markAsUnchecked();
        }

        alarmRepository.save(alarm);
        log.info("알람 확인 상태 변경 - 알람 ID: {}, 확인 여부: {}", alarmId, isChecked);

        return new AlarmResponse(alarm);
    }

    /**
     * 알람 삭제
     */
    public void deleteAlarm(Long userId, Long alarmId) throws AccessDeniedException {
        Alarm alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new IllegalArgumentException("알람을 찾을 수 없습니다."));

        if (!Objects.equals(alarm.getUser().getId(), userId)) {
            throw new AccessDeniedException("해당 알람에 대한 권한이 없습니다.");
        }

        alarmRepository.delete(alarm);
        log.info("알람 삭제 완료 - 사용자: {}, 알람 ID: {}", userId, alarmId);
    }

    /**
     * 확인하지 않은 알람 개수 조회
     */
    @Transactional(readOnly = true)
    public long getUncheckedAlarmCount(Long userId) {
        return alarmRepository.countByUserIdAndIsCheckedFalse(userId);
    }
}