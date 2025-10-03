package com.moodmate.api;

import com.moodmate.config.jwt.JwtTokenProvider;
import com.moodmate.domain.token.RefreshToken;
import com.moodmate.domain.token.RefreshTokenRepository;
import com.moodmate.domain.token.TokenType;
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

    public static String createRefreshToken(JwtTokenProvider provider, User user, RefreshTokenRepository refreshTokenRepository) {
        String token = provider.createRefreshToken(user.getId());
        refreshTokenRepository.save(new RefreshToken(user, token));
        return token;
    }

    public static String createAccessToken(JwtTokenProvider provider, User user) {
        return provider.createAccessToken(user);
    }

}
