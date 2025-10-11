package com.moodmate.controller;

import com.moodmate.domain.alarm.dto.AlarmListResponse;
import com.moodmate.domain.alarm.dto.AlarmRequest;
import com.moodmate.domain.alarm.dto.AlarmResponse;
import com.moodmate.domain.alarm.service.AlarmService;
import com.moodmate.domain.user.ouath.CustomOauth2User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.Map;

@Tag(name = "Alarm-Controller", description = "알람 관련 API")
@RestController
@RequestMapping("/api/alarm")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    @PostMapping
    @Operation(summary = "알람 저장",
            description = "새로운 알람을 생성합니다.",
            security = @SecurityRequirement(name = "bearer-key"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "알람 저장 성공",
                            content = @Content(schema = @Schema(implementation = AlarmResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content)
            }
    )
    public ResponseEntity<AlarmResponse> saveAlarm(
            @RequestBody @Valid AlarmRequest request,
            @AuthenticationPrincipal CustomOauth2User userDetails) {

        AlarmResponse response = alarmService.saveAlarm(
                userDetails.getUser().getId(),
                request
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "알람 목록 조회",
            description = "사용자의 알람 목록을 조회합니다. isChecked 파라미터로 필터링할 수 있습니다.",
            security = @SecurityRequirement(name = "bearer-key"),
            parameters = {
                    @Parameter(name = "isChecked", description = "확인 여부 (true: 확인한 알람, false: 확인하지 않은 알람, null: 전체)", required = false)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "알람 조회 성공",
                            content = @Content(schema = @Schema(implementation = AlarmListResponse.class))),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content)
            }
    )
    public ResponseEntity<AlarmListResponse> getAlarms(
            @RequestParam(required = false) Boolean isChecked,
            @AuthenticationPrincipal CustomOauth2User userDetails) {

        AlarmListResponse response = alarmService.getAlarms(
                userDetails.getUser().getId(),
                isChecked
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{alarmId}")
    @Operation(summary = "특정 알람 조회",
            description = "알람 ID로 특정 알람을 조회합니다.",
            security = @SecurityRequirement(name = "bearer-key"),
            parameters = {
                    @Parameter(name = "alarmId", description = "알람 ID", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "알람 조회 성공",
                            content = @Content(schema = @Schema(implementation = AlarmResponse.class))),
                    @ApiResponse(responseCode = "400", description = "존재하지 않는 알람", content = @Content),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
            }
    )
    public ResponseEntity<AlarmResponse> getAlarm(
            @PathVariable Long alarmId,
            @AuthenticationPrincipal CustomOauth2User userDetails) throws AccessDeniedException {

        AlarmResponse response = alarmService.getAlarm(
                userDetails.getUser().getId(),
                alarmId
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{alarmId}/check")
    @Operation(summary = "알람 확인 상태 변경",
            description = "알람의 확인 상태를 변경합니다.",
            security = @SecurityRequirement(name = "bearer-key"),
            parameters = {
                    @Parameter(name = "alarmId", description = "알람 ID", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "상태 변경 성공",
                            content = @Content(schema = @Schema(implementation = AlarmResponse.class))),
                    @ApiResponse(responseCode = "400", description = "존재하지 않는 알람", content = @Content),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
            }
    )
    public ResponseEntity<AlarmResponse> updateAlarmCheckedStatus(
            @PathVariable Long alarmId,
            @RequestBody Map<String, Boolean> body,
            @AuthenticationPrincipal CustomOauth2User userDetails) throws AccessDeniedException {

        boolean isChecked = body.getOrDefault("isChecked", true);

        AlarmResponse response = alarmService.updateAlarmCheckedStatus(
                userDetails.getUser().getId(),
                alarmId,
                isChecked
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{alarmId}")
    @Operation(summary = "알람 삭제",
            description = "특정 알람을 삭제합니다.",
            security = @SecurityRequirement(name = "bearer-key"),
            parameters = {
                    @Parameter(name = "alarmId", description = "알람 ID", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "알람 삭제 성공", content = @Content),
                    @ApiResponse(responseCode = "400", description = "존재하지 않는 알람", content = @Content),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
            }
    )
    public ResponseEntity<Void> deleteAlarm(
            @PathVariable Long alarmId,
            @AuthenticationPrincipal CustomOauth2User userDetails) throws AccessDeniedException {

        alarmService.deleteAlarm(
                userDetails.getUser().getId(),
                alarmId
        );

        return ResponseEntity.ok().build();
    }

    @GetMapping("/unchecked-count")
    @Operation(summary = "확인하지 않은 알람 개수 조회",
            description = "사용자의 확인하지 않은 알람 개수를 조회합니다.",
            security = @SecurityRequirement(name = "bearer-key"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content)
            }
    )
    public ResponseEntity<Map<String, Long>> getUncheckedAlarmCount(
            @AuthenticationPrincipal CustomOauth2User userDetails) {

        long count = alarmService.getUncheckedAlarmCount(userDetails.getUser().getId());

        return ResponseEntity.ok(Map.of("uncheckedCount", count));
    }
}