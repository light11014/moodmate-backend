// FeedbackController.java
package com.moodmate.controller;

import com.moodmate.domain.feedback.dto.*;
import com.moodmate.domain.feedback.service.AiFeedbackService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;

@Tag(name = "Feedback-Controller", description = "AI 피드백 관련 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FeedbackController {

    private final AiFeedbackService aiFeedbackService;

    @PostMapping("/diaries/{diaryId}/feedback")
    @Operation(summary = "AI 피드백 생성",
            description = "일기에 대한 AI 피드백을 생성합니다. 하루 최대 2회까지 이용 가능합니다.",
            security = @SecurityRequirement(name = "bearer-key"),
            parameters = {
                    @Parameter(name = "diaryId", description = "일기 ID", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "피드백 생성 성공",
                            content = @Content(schema = @Schema(implementation = FeedbackResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 (존재하지 않는 일기, 이미 피드백 존재 등)",
                            content = @Content),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자",
                            content = @Content),
                    @ApiResponse(responseCode = "403", description = "권한 없음 (타인의 일기)",
                            content = @Content),
                    @ApiResponse(responseCode = "429", description = "일일 사용량 초과",
                            content = @Content)
            }
    )
    public ResponseEntity<FeedbackResponse> generateFeedback(
            @PathVariable Long diaryId,
            @RequestBody @Valid FeedbackStyleRequest request,
            @AuthenticationPrincipal CustomOauth2User userDetails) throws AccessDeniedException {

        FeedbackResponse response = aiFeedbackService.createFeedback(
                userDetails.getUser().getId(),
                diaryId,
                request
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/diaries/{diaryId}/feedback")
    @Operation(summary = "특정 일기의 피드백 조회",
            security = @SecurityRequirement(name = "bearer-key"),
            parameters = {
                    @Parameter(name = "diaryId", description = "일기 ID", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "피드백 조회 성공",
                            content = @Content(schema = @Schema(implementation = FeedbackResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 (존재하지 않는 일기/피드백)",
                            content = @Content),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자",
                            content = @Content),
                    @ApiResponse(responseCode = "403", description = "권한 없음",
                            content = @Content)
            }
    )
    public ResponseEntity<FeedbackResponse> getFeedback(
            @PathVariable Long diaryId,
            @AuthenticationPrincipal CustomOauth2User userDetails) throws AccessDeniedException {

        FeedbackResponse response = aiFeedbackService.getFeedback(
                userDetails.getUser().getId(),
                diaryId
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/feedback/usage")
    @Operation(summary = "피드백 사용량 조회",
            description = "오늘 사용한 피드백 횟수와 남은 횟수를 확인합니다.",
            security = @SecurityRequirement(name = "bearer-key"),
            parameters = {
                    @Parameter(name = "type", description = "사용량 타입", required = true, example = "daily")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "사용량 조회 성공",
                            content = @Content(schema = @Schema(implementation = DailyUsageResponse.class))),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자",
                            content = @Content)
            }
    )
    public ResponseEntity<DailyUsageResponse> getFeedbackUsage(
            @RequestParam String type,
            @AuthenticationPrincipal CustomOauth2User userDetails) {

        if (!"daily".equals(type)) {
            throw new IllegalArgumentException("현재는 daily 타입만 지원합니다.");
        }

        DailyUsageResponse response = aiFeedbackService.getDailyUsage(
                userDetails.getUser().getId()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/feedback/history")
    @Operation(summary = "피드백 이력 조회",
            description = "지정된 기간 내의 피드백 이력을 조회합니다.",
            security = @SecurityRequirement(name = "bearer-key"),
            parameters = {
                    @Parameter(name = "startDate", description = "조회 시작 날짜", required = true, example = "2025-09-01"),
                    @Parameter(name = "endDate", description = "조회 종료 날짜", required = true, example = "2025-10-01")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "이력 조회 성공",
                            content = @Content(schema = @Schema(implementation = FeedbackHistoryResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 날짜 형식",
                            content = @Content),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자",
                            content = @Content)
            }
    )
    public ResponseEntity<FeedbackHistoryResponse> getFeedbackHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal CustomOauth2User userDetails) {

        FeedbackHistoryResponse response = aiFeedbackService.getFeedbackHistory(
                userDetails.getUser().getId(),
                startDate,
                endDate
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/feedback/statistics")
    @Operation(
            summary = "피드백 종합 분석",
            description = "지정된 기간 내의 피드백을 종합적으로 분석합니다.",
            security = @SecurityRequirement(name = "bearer-key"),
            parameters = {
                    @Parameter(name = "startDate", description = "조회 시작 날짜", required = true, example = "2025-09-01"),
                    @Parameter(name = "endDate", description = "조회 종료 날짜", required = true, example = "2025-10-01")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "분석 성공",
                            content = @Content(schema = @Schema(implementation = PeriodAnalysisResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 날짜 형식",
                            content = @Content),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자",
                            content = @Content)
            }
    )
    public ResponseEntity<PeriodAnalysisResponse> getFeedbackStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal CustomOauth2User userDetails
    ) {
        // startDate, endDate를 DTO로 변환
        PeriodAnalysisRequest request = new PeriodAnalysisRequest();
        request.setStartDate(startDate);
        request.setEndDate(endDate);

        PeriodAnalysisResponse response = aiFeedbackService.generatePeriodAnalysis(
                userDetails.getUser().getId(),
                request
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/diaries/{diaryId}/feedback")
    @Operation(summary = "피드백 삭제",
            description = "특정 일기의 피드백을 삭제합니다.",
            security = @SecurityRequirement(name = "bearer-key"),
            parameters = {
                    @Parameter(name = "diaryId", description = "일기 ID", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "피드백 삭제 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 (존재하지 않는 일기/피드백)",
                            content = @Content),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자",
                            content = @Content),
                    @ApiResponse(responseCode = "403", description = "권한 없음 (타인의 피드백)",
                            content = @Content)
            }
    )
    public ResponseEntity<Void> deleteFeedback(
            @PathVariable Long diaryId,
            @AuthenticationPrincipal CustomOauth2User userDetails) throws AccessDeniedException {

        aiFeedbackService.deleteFeedback(
                userDetails.getUser().getId(),
                diaryId
        );

        return ResponseEntity.ok().build();
    }
}