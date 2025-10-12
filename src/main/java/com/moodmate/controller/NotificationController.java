package com.moodmate.controller;

import com.moodmate.domain.notification.dto.NotificationListResponse;
import com.moodmate.domain.notification.dto.NotificationRequest;
import com.moodmate.domain.notification.dto.NotificationResponse;
import com.moodmate.domain.notification.service.NotificationService;
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

@Tag(name = "Notification-Controller", description = "알림 관련 API")
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @Operation(summary = "알림 저장",
            description = "새로운 알림을 생성합니다.",
            security = @SecurityRequirement(name = "bearer-key"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "알림 저장 성공",
                            content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content)
            }
    )
    public ResponseEntity<NotificationResponse> saveNotification(
            @RequestBody @Valid NotificationRequest request,
            @AuthenticationPrincipal CustomOauth2User userDetails) {

        NotificationResponse response = notificationService.saveNotification(
                userDetails.getUser().getId(),
                request
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "알림 목록 조회",
            description = "사용자의 알림 목록을 조회합니다. isChecked 파라미터로 필터링할 수 있습니다.",
            security = @SecurityRequirement(name = "bearer-key"),
            parameters = {
                    @Parameter(name = "isChecked", description = "확인 여부 (true: 확인한 알림, false: 확인하지 않은 알림, null: 전체)", required = false)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "알림 조회 성공",
                            content = @Content(schema = @Schema(implementation = NotificationListResponse.class))),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content)
            }
    )
    public ResponseEntity<NotificationListResponse> getNotifications(
            @RequestParam(required = false) Boolean isChecked,
            @AuthenticationPrincipal CustomOauth2User userDetails) {

        NotificationListResponse response = notificationService.getNotifications(
                userDetails.getUser().getId(),
                isChecked
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{notificationId}")
    @Operation(summary = "특정 알림 조회",
            description = "알림 ID로 특정 알림을 조회합니다.",
            security = @SecurityRequirement(name = "bearer-key"),
            parameters = {
                    @Parameter(name = "notificationId", description = "알림 ID", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "알림 조회 성공",
                            content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
                    @ApiResponse(responseCode = "400", description = "존재하지 않는 알람", content = @Content),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
            }
    )
    public ResponseEntity<NotificationResponse> getNotification(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal CustomOauth2User userDetails) throws AccessDeniedException {

        NotificationResponse response = notificationService.getNotification(
                userDetails.getUser().getId(),
                notificationId
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{notificationId}/check")
    @Operation(summary = "알림 확인 상태 변경",
            description = "알림의 확인 상태를 변경합니다.",
            security = @SecurityRequirement(name = "bearer-key"),
            parameters = {
                    @Parameter(name = "notificationId", description = "알람 ID", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "상태 변경 성공",
                            content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
                    @ApiResponse(responseCode = "400", description = "존재하지 않는 알람", content = @Content),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
            }
    )
    public ResponseEntity<NotificationResponse> updateNotificationCheckedStatus(
            @PathVariable Long notificationId,
            @RequestBody Map<String, Boolean> body,
            @AuthenticationPrincipal CustomOauth2User userDetails) throws AccessDeniedException {

        boolean isChecked = body.getOrDefault("isChecked", true);

        NotificationResponse response = notificationService.updateNotificationCheckedStatus(
                userDetails.getUser().getId(),
                notificationId,
                isChecked
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "알림 삭제",
            description = "특정 알림을 삭제합니다.",
            security = @SecurityRequirement(name = "bearer-key"),
            parameters = {
                    @Parameter(name = "notificationId", description = "알림 ID", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "알림 삭제 성공", content = @Content),
                    @ApiResponse(responseCode = "400", description = "존재하지 않는 알람", content = @Content),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
            }
    )
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal CustomOauth2User userDetails) throws AccessDeniedException {

        notificationService.deleteNotification(
                userDetails.getUser().getId(),
                notificationId
        );

        return ResponseEntity.ok().build();
    }

    @GetMapping("/unchecked-count")
    @Operation(summary = "확인하지 않은 알림 개수 조회",
            description = "사용자의 확인하지 않은 알림 개수를 조회합니다.",
            security = @SecurityRequirement(name = "bearer-key"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content)
            }
    )
    public ResponseEntity<Map<String, Long>> getUncheckedNotificationCount(
            @AuthenticationPrincipal CustomOauth2User userDetails) {

        long count = notificationService.getUncheckedNotificationCount(userDetails.getUser().getId());

        return ResponseEntity.ok(Map.of("uncheckedCount", count));
    }
}