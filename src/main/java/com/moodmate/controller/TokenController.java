package com.moodmate.controller;

import com.moodmate.domain.token.RefreshToken;
import com.moodmate.domain.token.dto.AccessTokenResponse;
import com.moodmate.domain.token.service.RefreshTokenService;
import com.moodmate.domain.token.service.TokenService;
import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "토큰", description = "Access Token API")
@RequiredArgsConstructor
@RestController
public class TokenController {
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/api/auth/refresh")
    public ResponseEntity<?> createNewAccessToken(
            @CookieValue(value = "mm-rt", required = false) String refreshToken) {

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Wrong Token."));
        }

        try {
            String newAccessToken = tokenService.createNewAccessToken(refreshToken);
            return ResponseEntity.ok(new AccessTokenResponse(newAccessToken));
        } catch (ExpiredJwtException e) {
            refreshTokenService.deleteByRefreshToken(refreshToken);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "RefreshToken expired. Please login again."));
        }
    }
}
