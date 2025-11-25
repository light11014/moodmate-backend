package com.moodmate.controller;

import com.moodmate.domain.feedback.dto.*;
import com.moodmate.domain.feedback.service.PeriodAnalysisService;
import com.moodmate.domain.user.ouath.CustomOauth2User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/feedback/period-analysis")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Period Analysis", description = "기간별 종합 피드백 API")
public class PeriodAnalysisController {

    private final PeriodAnalysisService periodAnalysisService;

    @Operation(summary = "종합 피드백 생성",
            description = "지정된 기간의 일기들을 종합 분석하여 피드백을 생성하고 DB에 저장합니다.",
            security = @SecurityRequirement(name = "bearer-key"),
            parameters = {
                    @Parameter(name = "startDate", description = "시작 날짜", required = true, example = "2025-04-01"),
                    @Parameter(name = "endDate", description = "종료 날짜", required = true, example = "2025-04-30"),
                    @Parameter(name = "overwrite", description = "덮어쓰기 여부", required = false, example = "false")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "종합 피드백 생성 성공",
                            content = @Content(schema = @Schema(implementation = PeriodAnalysisDetailResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content)
            })
    @PostMapping
    public ResponseEntity<PeriodAnalysisDetailResponse> createPeriodAnalysis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "false") boolean overwrite,
            @AuthenticationPrincipal CustomOauth2User userDetails) throws Exception {

        Long userId = userDetails.getUser().getId();
        log.info("종합 피드백 생성 요청 - 사용자: {}, 기간: {} ~ {}, 덮어쓰기: {}",
                userId, startDate, endDate, overwrite);

        CreatePeriodAnalysisRequest request = new CreatePeriodAnalysisRequest(startDate, endDate, overwrite);
        PeriodAnalysisDetailResponse response = periodAnalysisService.createPeriodAnalysis(userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "종합 피드백 상세 조회",
            description = "저장된 종합 피드백의 상세 내용을 조회합니다.",
            security = @SecurityRequirement(name = "bearer-key"),
            parameters = {
                    @Parameter(name = "analysisId", description = "분석 ID", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = PeriodAnalysisDetailResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
            })
    @GetMapping("/{analysisId}")
    public ResponseEntity<PeriodAnalysisDetailResponse> getPeriodAnalysis(
            @PathVariable Long analysisId,
            @AuthenticationPrincipal CustomOauth2User userDetails) throws AccessDeniedException {

        Long userId = userDetails.getUser().getId();
        log.info("종합 피드백 조회 요청 - 사용자: {}, 분석 ID: {}", userId, analysisId);

        PeriodAnalysisDetailResponse response = periodAnalysisService.getPeriodAnalysis(userId, analysisId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 모든 종합 피드백 목록 조회",
            description = "사용자의 모든 종합 피드백 목록을 조회합니다.",
            security = @SecurityRequirement(name = "bearer-key"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "목록 조회 성공",
                            content = @Content(schema = @Schema(implementation = PeriodAnalysisListResponse.class))),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content)
            })
    @GetMapping
    public ResponseEntity<PeriodAnalysisListResponse> getAllPeriodAnalyses(
            @AuthenticationPrincipal CustomOauth2User userDetails) {

        Long userId = userDetails.getUser().getId();
        log.info("종합 피드백 목록 조회 요청 - 사용자: {}", userId);

        PeriodAnalysisListResponse response = periodAnalysisService.getAllPeriodAnalyses(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "특정 기간의 종합 피드백 조회",
            description = "특정 기간에 대한 종합 피드백이 있는지 조회합니다.",
            security = @SecurityRequirement(name = "bearer-key"),
            parameters = {
                    @Parameter(name = "startDate", description = "시작 날짜", required = true, example = "2025-04-01"),
                    @Parameter(name = "endDate", description = "종료 날짜", required = true, example = "2025-04-30")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = PeriodAnalysisDetailResponse.class))),
                    @ApiResponse(responseCode = "404", description = "해당 기간의 종합 피드백이 없음", content = @Content),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content)
            })
    @GetMapping("/by-period")
    public ResponseEntity<PeriodAnalysisDetailResponse> getPeriodAnalysisByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal CustomOauth2User userDetails) {

        Long userId = userDetails.getUser().getId();
        log.info("특정 기간 종합 피드백 조회 요청 - 사용자: {}, 기간: {} ~ {}",
                userId, startDate, endDate);

        Optional<PeriodAnalysisDetailResponse> response =
                periodAnalysisService.getPeriodAnalysisByPeriod(userId, startDate, endDate);

        return response.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "종합 피드백 삭제",
            description = "저장된 종합 피드백을 삭제합니다.",
            security = @SecurityRequirement(name = "bearer-key"),
            parameters = {
                    @Parameter(name = "analysisId", description = "분석 ID", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "삭제 성공", content = @Content),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
            })
    @DeleteMapping("/{analysisId}")
    public ResponseEntity<Void> deletePeriodAnalysis(
            @PathVariable Long analysisId,
            @AuthenticationPrincipal CustomOauth2User userDetails) throws AccessDeniedException {

        Long userId = userDetails.getUser().getId();
        log.info("종합 피드백 삭제 요청 - 사용자: {}, 분석 ID: {}", userId, analysisId);

        periodAnalysisService.deletePeriodAnalysis(userId, analysisId);
        return ResponseEntity.noContent().build();
    }
}