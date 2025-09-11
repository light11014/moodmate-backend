package com.moodmate.api;

import com.moodmate.common.JwtUtil;
import com.moodmate.domain.user.UserRepository;
import com.moodmate.domain.user.entity.Role;
import com.moodmate.domain.user.entity.User;

public class TestUtils {

    public static User createUser(UserRepository userRepository) {
        return userRepository.save(User.builder()
                .username("테스트유저")
                .role(Role.USER)
                .loginId("google_123")
                .provider("google")
                .providerId("123")
                .email("test@example.com")
                .pictureUrl("http://example.com/img.png")
                .build());
    }

    public static String createToken(JwtUtil provider, User user) {
        return provider.createToken(
                user.getId(),
                user.getEmail(),
                Role.USER
        );
    }
}
