package com.moodmate.controller;

import com.moodmate.domain.diary.dto.DiaryMonthSummaryResponse;
import com.moodmate.domain.tracking.TrackingService;
import com.moodmate.domain.tracking.dto.EmotionFrequency;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Tracking-Controller", description = "감정 트래킹 관련 API")
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @GetMapping("/emotions/frequency")
    @Operation(summary = "기간별 감정 빈도 조회",
            security = @SecurityRequirement(name = "bearer-key"),
            parameters = {
                    @Parameter(name = "startDate", description = "조회 시작 날짜", required = true, example = "2025-09-01"),
                    @Parameter(name = "endDate", description = "조회 종료 날짜", required = true, example = "2025-10-01")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = EmotionFrequency.class)))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청(잘못된 기간 설정 등)", content = @Content),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content)
            }
    )
    public List<EmotionFrequency> getEmotionFrequency(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal CustomOauth2User userDetails
    ) {
        return trackingService.getEmotionFrequency(userDetails.getUser().getId(), startDate, endDate);
    }
}
