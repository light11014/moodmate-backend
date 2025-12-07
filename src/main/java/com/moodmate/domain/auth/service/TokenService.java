package com.moodmate.domain.auth.service;

import com.moodmate.config.security.jwt.JwtTokenProvider;
import com.moodmate.domain.auth.entity.TokenType;
import com.moodmate.domain.user.entity.User;
import com.moodmate.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TokenService {
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    @Transactional
    public String createNewAccessToken(String refreshToken) {
        jwtTokenProvider.validateToken(refreshToken, TokenType.REFRESH);

        Long userId = refreshTokenService.findByRefreshToken(refreshToken).getUser().getId();
        User user = userService.findById(userId);

        return jwtTokenProvider.createAccessToken(user);
    }
}
