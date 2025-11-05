package com.moodmate.api;

import com.moodmate.config.encryption.EncryptionKeyService;
import com.moodmate.config.jwt.JwtTokenProvider;
import com.moodmate.domain.token.RefreshToken;
import com.moodmate.domain.token.RefreshTokenRepository;
import com.moodmate.domain.user.UserRepository;
import com.moodmate.domain.user.entity.Role;
import com.moodmate.domain.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TestUtils {
    private final EncryptionKeyService keyService;

    public TestUtils(EncryptionKeyService keyService) {
        this.keyService = keyService;
    }

    /**
     * 테스트용 사용자 생성
     */
    public User createUser(UserRepository userRepository) {
        try {
            String encryptedDek = keyService.createAndEncryptDek();

            return userRepository.save(User.createOAuthUser(
                    "google_123",
                    "google",
                    "123",
                    Role.USER,
                    null,
                    "test123@example.com",
                    encryptedDek
            ));
        } catch (Exception e) {
            throw new RuntimeException("암호화 키 생성 오류", e);
        }
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
