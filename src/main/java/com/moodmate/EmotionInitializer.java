package com.moodmate;

import com.moodmate.domain.emotion.Emotion;
import com.moodmate.domain.emotion.EmotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EmotionInitializer implements CommandLineRunner {

    private final EmotionRepository emotionRepository;

    @Override
    public void run(String... args) {
        List<String> defaultEmotions = List.of("기쁨", "슬픔", "분노", "우울", "뿌듯", "놀람");

        for (String name : defaultEmotions) {
            emotionRepository.findByName(name)
                    .orElseGet(() -> emotionRepository.save(new Emotion(name)));
        }
    }
}
