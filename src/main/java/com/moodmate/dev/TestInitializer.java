package com.moodmate.dev;

import com.moodmate.domain.emotion.Emotion;
import com.moodmate.domain.emotion.EmotionRepository;
import com.moodmate.domain.user.UserRepository;
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

    @Override
    public void run(String... args) {
        // --- test User 생성 ---
        User user = User.builder()
                .loginId("moodmate001")
                .provider("moodmate")
                .providerId("001")
                .role(Role.USER)
                .email("test@example.com")
                .username("test")
                .build();
        userRepository.findByEmail("test@example.com")
                .orElseGet(() -> userRepository.save(user));

        // --- Emotion 초기화 ---
        List<String> defaultEmotions = List.of("기쁨", "슬픔", "분노", "우울", "뿌듯", "놀람");

        for (String name : defaultEmotions) {
            emotionRepository.findByName(name)
                    .orElseGet(() -> emotionRepository.save(new Emotion(name)));
        }
    }
}