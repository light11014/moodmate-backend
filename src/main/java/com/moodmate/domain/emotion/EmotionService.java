package com.moodmate.domain.emotion;

import com.moodmate.domain.emotion.dto.EmotionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmotionService {

    private final EmotionRepository emotionRepository;

    public List<EmotionResponse> getAllEmotions() {
        return emotionRepository.findAll().stream()
                .map(e -> new EmotionResponse(e.getId(), e.getName()))
                .collect(Collectors.toList());
    }
}
