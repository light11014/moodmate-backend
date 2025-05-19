package com.moodmate.domain.diary;

import com.moodmate.domain.diary.dto.DiaryRequestDto;
import com.moodmate.domain.user.ouath.CustomOauth2User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diaries")
public class DiaryController {

    private final DiaryService diaryService;

    @PostMapping
    public ResponseEntity<Long> saveDiary(@RequestBody @Valid DiaryRequestDto dto,
                                          @AuthenticationPrincipal CustomOauth2User userDetails) {
        Long savedId = diaryService.saveDiary(dto, userDetails.getUser().getId());
        return ResponseEntity.ok(savedId);
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
