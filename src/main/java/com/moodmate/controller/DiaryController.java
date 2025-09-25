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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.YearMonth;
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

    @GetMapping("/monthly")
    @Operation(summary = "월별 일기 조회",
            security = @SecurityRequirement(name = "bearer-key"),
            parameters = {
                    @Parameter(name = "date", description = "조회할 날짜", required = true, example = "2025-05")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "월별 일기 조회 성공",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = DiaryMonthSummaryResponse.class)))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content)
            }
    )
    public ResponseEntity<List<DiaryMonthSummaryResponse>> getMonthlyDiaries(
            @RequestParam("date") String dateStr,
            @AuthenticationPrincipal CustomOauth2User userDetails) {

        YearMonth yearMonth = YearMonth.parse(dateStr);
        List<DiaryMonthSummaryResponse> summaries = diaryService.getDiarySummariesByMonth(
                userDetails.getUser().getId(), yearMonth
        );

        return ResponseEntity.ok(summaries);
    }

    @Operation(summary = "날짜별 일기 조회",
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
            @RequestParam("date") String dateStr,
            @AuthenticationPrincipal CustomOauth2User userDetails) {
        LocalDate date = LocalDate.parse(dateStr);
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
