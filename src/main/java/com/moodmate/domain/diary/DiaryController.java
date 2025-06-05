package com.moodmate.domain.diary;

import com.moodmate.domain.diary.dto.DiaryMonthSummaryDto;
import com.moodmate.domain.diary.dto.DiaryRequestDto;
import com.moodmate.domain.diary.dto.DiaryResponseDto;
import com.moodmate.domain.user.ouath.CustomOauth2User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diaries")
public class DiaryController {

    private final DiaryService diaryService;

    @PostMapping
    public ResponseEntity<Long> saveDiary(@RequestBody @Valid DiaryRequestDto dto,
                                          @AuthenticationPrincipal CustomOauth2User userDetails) {
        Long savedId = diaryService.saveDiary(userDetails.getUser().getId(), dto);
        return ResponseEntity.ok(savedId);
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<DiaryMonthSummaryDto>> getMonthlyDiaries(
            @RequestParam("date") String dateStr,
            @AuthenticationPrincipal CustomOauth2User userDetails) {
        YearMonth yearMonth = YearMonth.parse(dateStr); // "2025-05"
        List<DiaryMonthSummaryDto> summaries = diaryService.getDiarySummariesByMonth(userDetails.getUser().getId(), yearMonth);
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/by-date")
    public ResponseEntity<DiaryResponseDto> getDiaryByDate(
            @RequestParam("date") String dateStr,
            @AuthenticationPrincipal CustomOauth2User userDetails) {
        LocalDate date = LocalDate.parse(dateStr); // "2025-05-12"
        DiaryResponseDto response = diaryService.getDiaryByDate(userDetails.getUser().getId(), date);
        return ResponseEntity.ok(response);
    }




    @PutMapping("/{diaryId}")
    public ResponseEntity<?> updateDiary(@PathVariable Long diaryId,
                                         @RequestBody @Valid DiaryRequestDto dto,
                                         @AuthenticationPrincipal CustomOauth2User userDetails) throws AccessDeniedException {
        diaryService.updateDiary(diaryId, dto, userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{diaryId}")
    public ResponseEntity<?> deleteDiary(@PathVariable Long diaryId,
                                         @AuthenticationPrincipal CustomOauth2User userDetails) throws AccessDeniedException {
        diaryService.deleteDiary(diaryId, userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }
}
