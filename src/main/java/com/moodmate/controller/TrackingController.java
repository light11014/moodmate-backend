package com.moodmate.controller;

import com.moodmate.domain.tracking.TrackingService;
import com.moodmate.domain.tracking.dto.frequency.EmotionFrequencyResponse;
import com.moodmate.domain.tracking.dto.ratio.EmotionRatioResponse;
import com.moodmate.domain.tracking.dto.trend.EmotionTrendResponse;
import com.moodmate.domain.user.ouath.CustomOauth2User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Tag(name = "Tracking-Controller", description = "감정 트래킹 관련 API")
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @GetMapping("/emotions/frequency")
    @Operation(summary = "기간별 감정 빈도 조회",
            parameters = {
                    @Parameter(name = "startDate", description = "조회 시작 날짜", required = true, example = "2025-09-01"),
                    @Parameter(name = "endDate", description = "조회 종료 날짜", required = true, example = "2025-10-01")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = EmotionFrequencyResponse.class)))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청(잘못된 기간 설정 등)", content = @Content),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content)
            }
    )
    public ResponseEntity<EmotionFrequencyResponse> getEmotionFrequency(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal CustomOauth2User userDetails
    ) {
        return ResponseEntity.ok(trackingService.getEmotionFrequency(userDetails.getUser().getId(), startDate, endDate));
    }

    @GetMapping("/emotions/ratio")
    @Operation(summary = "기간별 감정 강도 비율 조회",
            parameters = {
                    @Parameter(name = "startDate", description = "조회 시작 날짜", required = true, example = "2025-09-01"),
                    @Parameter(name = "endDate", description = "조회 종료 날짜", required = true, example = "2025-10-01")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = EmotionRatioResponse.class)))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청(잘못된 기간 설정 등)", content = @Content),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content)
            }
    )
    public ResponseEntity<EmotionRatioResponse> getEmotionIntensityRatio(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal CustomOauth2User userDetails
    ) {
        return ResponseEntity.ok(trackingService.getEmotionRatio(userDetails.getUser().getId(), startDate, endDate));
    }

    @GetMapping("/emotions/trend")
    @Operation(summary = "기간별 감정 추세 조회",
            description = "콤마(,)로 구분된 감정 목록을 전달하면 해당 감정만 조회합니다. 비우면 전체 조회.",
            parameters = {
                    @Parameter(name = "startDate", description = "조회 시작 날짜", required = true, example = "2025-09-01"),
                    @Parameter(name = "endDate", description = "조회 종료 날짜", required = true, example = "2025-10-01"),
                    @Parameter(name = "emotions", description = "조회할 감정 목록 (공백 없이 콤마 구분)", example = "기쁨,슬픔")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = EmotionTrendResponse.class)))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청(잘못된 기간 설정 등)", content = @Content),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content)
            }
    )
    public ResponseEntity<EmotionTrendResponse> getEmotionTrend(
            @AuthenticationPrincipal CustomOauth2User userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String emotions
    ) {
        List<String> emotionList = (emotions == null || emotions.isBlank())
                ? Collections.emptyList()
                : Arrays.stream(emotions.split(","))
                .map(String::trim)
                .toList();

        EmotionTrendResponse response = trackingService.getEmotionTrend(
                userDetails.getUser().getId(),
                startDate,
                endDate,
                emotionList
        );

        return ResponseEntity.ok(response);
    }
}
