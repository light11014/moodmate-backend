package com.moodmate.domain.auth.controller;

import com.moodmate.domain.auth.dto.AccessTokenResponse;
import com.moodmate.domain.auth.service.RefreshTokenService;
import com.moodmate.domain.auth.service.TokenService;
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

        } catch (IllegalArgumentException e) {
            // 검증 실패, 타입 불일치
            if (e.getMessage().contains("Wrong token type")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid token type"));
            }

            // DB에서 못 찾음 (로그아웃됨)
            if (e.getMessage().contains("RefreshToken not found")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token not found. Please login again."));
            }

            // 기타
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid refresh token"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Token refresh failed"));
        }


    }




}
