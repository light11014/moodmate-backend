package com.moodmate.domain.token.service;

import com.moodmate.config.jwt.JwtTokenProvider;
import com.moodmate.domain.token.TokenType;
import com.moodmate.domain.user.entity.User;
import com.moodmate.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    public String createNewAccessToken(String refreshToken) {
        if(!jwtTokenProvider.validateToken(refreshToken, TokenType.REFRESH)) {
            throw new IllegalArgumentException("Unexpected token");
        }

        Long userId = refreshTokenService.findByRefreshToken(refreshToken).getUser().getId();
        User user = userService.findById(userId);

        return jwtTokenProvider.createAccessToken(user);
    }
}
