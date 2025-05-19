package com.moodmate.domain.emotion;

import com.moodmate.domain.emotion.dto.EmotionResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmotionService {

    private final EmotionRepository emotionRepository;

    public List<EmotionResponseDto> getAllEmotions() {
        return emotionRepository.findAll().stream()
                .map(e -> new EmotionResponseDto(e.getId(), e.getName()))
                .collect(Collectors.toList());
    }
}
