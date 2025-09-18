package com.moodmate.api;

import com.moodmate.common.JwtUtil;
import com.moodmate.domain.user.UserRepository;
import com.moodmate.domain.user.entity.Role;
import com.moodmate.domain.user.entity.User;

public class TestUtils {

    public static User createUser(UserRepository userRepository) {
        return userRepository.save(User.createOAuthUser(
                "google_123",
                "google",
                "123",
                Role.USER,
                null,
                "test123@example.com"));
    }

    public static String createToken(JwtUtil provider, User user) {
        return provider.createToken(
                user.getId(),
                user.getEmail(),
                Role.USER
        );
    }
}
