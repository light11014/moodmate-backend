package com.moodmate.dev;

import com.moodmate.config.encryption.EncryptionKeyService;
import com.moodmate.domain.emotion.entity.Emotion;
import com.moodmate.domain.emotion.repository.EmotionRepository;
import com.moodmate.domain.user.repository.UserRepository;
import com.moodmate.domain.user.entity.Role;
import com.moodmate.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class TestInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final EmotionRepository emotionRepository;

    private final EncryptionKeyService keyService;

    @Override
    public void run(String... args) throws Exception {
        // --- Emotion 초기화 ---
        List<String> defaultEmotions = List.of("열정", "신남", "설렘", "편안", "기쁨", "뿌듯", "화남", "답답", "불안", "슬픔", "부끄", "놀람");

        for (String name : defaultEmotions) {
            emotionRepository.findByName(name)
                    .orElseGet(() -> emotionRepository.save(new Emotion(name)));
        }
    }
}