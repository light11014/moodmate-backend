package com.moodmate.domain.emotion;

import com.moodmate.domain.emotion.dto.EmotionResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/emotions")
public class EmotionController {

    private final EmotionService emotionService;

    @GetMapping
    public ResponseEntity<List<EmotionResponseDto>> getEmotions() {
        return ResponseEntity.ok(emotionService.getAllEmotions());
    }
}

