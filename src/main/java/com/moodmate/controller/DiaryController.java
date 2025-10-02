package com.moodmate.controller;

import com.moodmate.domain.diary.DiaryService;
import com.moodmate.domain.diary.dto.DiaryMonthSummaryResponse;
import com.moodmate.domain.diary.dto.DiaryRequest;
import com.moodmate.domain.diary.dto.DiaryResponse;
import com.moodmate.domain.user.ouath.CustomOauth2User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
import java.util.List;

@Tag(name = "감정 일기", description = "Diary API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diaries")
public class DiaryController {

    private final DiaryService diaryService;

    @PostMapping
    @Operation(summary = "일기 작성",
            security = @SecurityRequirement(name = "bearer-key"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "일기 저장 성공",
                            content = @Content(examples = @ExampleObject(value = "\"diary_id\""))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청(지원하지 않는 감정 등)", content = @Content),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content)
            }
    )
    public ResponseEntity<Long> saveDiary(@RequestBody @Schema(implementation = DiaryRequest.class) @Valid DiaryRequest dto,
                                          @AuthenticationPrincipal CustomOauth2User userDetails) {
        Long savedId = diaryService.saveDiary(userDetails.getUser().getId(), dto);
        return ResponseEntity.ok(savedId);
    }

    @GetMapping("/period")
    @Operation(summary = "기간별 일기 조회",
            description = "지정된 기간의 일기를 조회합니다. require 파라미터로 요약만 볼지 전체 내용을 볼지 선택할 수 있습니다.",
            security = @SecurityRequirement(name = "bearer-key"),
            parameters = {
                    @Parameter(name = "startDate", description = "조회 시작 날짜", required = true, example = "2025-07-01"),
                    @Parameter(name = "endDate", description = "조회 종료 날짜", required = true, example = "2025-07-05"),
                    @Parameter(name = "require", description = "응답 형식 (summary: 요약, full: 전체)", required = false, example = "summary")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "기간별 일기 조회 성공 (require=summary인 경우)",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = DiaryMonthSummaryResponse.class)))),
                    @ApiResponse(responseCode = "200", description = "기간별 일기 조회 성공 (require=full인 경우)",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = DiaryResponse.class)))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content)
            }
    )
    public ResponseEntity<?> getDiariesByPeriod(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "require", required = false, defaultValue = "summary") String require,
            @AuthenticationPrincipal CustomOauth2User userDetails) {

        // 날짜 검증
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 이전이어야 합니다.");
        }

        if ("full".equals(require)) {
            List<DiaryResponse> fullDiaries = diaryService.getDiariesByPeriodFull(
                    userDetails.getUser().getId(), startDate, endDate
            );
            return ResponseEntity.ok(fullDiaries);
        } else {
            List<DiaryMonthSummaryResponse> summaries = diaryService.getDiariesByPeriodSummary(
                    userDetails.getUser().getId(), startDate, endDate
            );
            return ResponseEntity.ok(summaries);
        }
    }

    @Operation(summary = "날짜별 일기 조회",
            description = "특정 날짜의 일기를 조회합니다. 작성 시간과 수정 시간을 포함합니다.",
            security = @SecurityRequirement(name = "bearer-key"),
            parameters = {
                    @Parameter(name = "date", description = "조회할 날짜", required = true, example = "2025-05-12")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "일기 조회 성공",
                            content = @Content(schema = @Schema(implementation = DiaryResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content)
            }
    )
    @GetMapping("/by-date")
    public ResponseEntity<DiaryResponse> getDiaryByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal CustomOauth2User userDetails) {
        DiaryResponse response = diaryService.getDiaryByDate(userDetails.getUser().getId(), date);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "일기 수정",
            security = @SecurityRequirement(name = "bearer-key"),
            parameters = {
                    @Parameter(name = "diaryId", description = "수정할 일기의 Id", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "일기 수정 성공",
                            content = @Content(schema = @Schema(implementation = DiaryResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청(이미 작성된 날짜로 변경)", content = @Content),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content)
            }
    )
    @PutMapping("/{diaryId}")
    public ResponseEntity<?> updateDiary(@PathVariable Long diaryId,
                                         @RequestBody @Schema(implementation = DiaryRequest.class) @Valid DiaryRequest dto,
                                         @AuthenticationPrincipal CustomOauth2User userDetails) throws AccessDeniedException {
        diaryService.updateDiary(diaryId, dto, userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "일기 삭제",
            security = @SecurityRequirement(name = "bearer-key"),
            parameters = {
                    @Parameter(name = "diaryId", description = "삭제할 일기의 Id", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "일기 삭제 성공", content = @Content),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content)
            }
    )
    @DeleteMapping("/{diaryId}")
    public ResponseEntity<?> deleteDiary(@PathVariable Long diaryId,
                                         @AuthenticationPrincipal CustomOauth2User userDetails) throws AccessDeniedException {
        diaryService.deleteDiary(diaryId, userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }
}