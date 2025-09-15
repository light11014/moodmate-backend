package com.moodmate.controller;

import com.moodmate.domain.emotion.EmotionService;
import com.moodmate.domain.emotion.dto.EmotionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Emotion-Controller", description = "감정 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/emotions")
public class EmotionController {

    private final EmotionService emotionService;

    @GetMapping
    @Operation(summary = "감정 목록 조회",
            security = @SecurityRequirement(name = "bearer-key"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "감정 목록 조회 성공",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = EmotionResponse.class)))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content)
            }
    )
    public ResponseEntity<List<EmotionResponse>> getEmotions() {
        return ResponseEntity.ok(emotionService.getAllEmotions());
    }
}

