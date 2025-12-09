package com.moodmate.domain.auth.controller;

import com.moodmate.config.security.ouath.CustomOauth2User;
import com.moodmate.domain.auth.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "인증", description = "Auth API")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth")
public class AuthController {
    private final RefreshTokenService refreshTokenService;

    @Operation(
            summary = "소셜 로그인 시작",
            description = "구글/카카오 등 소셜 로그인을 시작합니다. 실제 요청 시 Spring Security OAuth2 Login이 처리합니다."
    )
    @GetMapping("/login/{provider}")
    public ResponseEntity<Void> login(
            @Parameter(description = "로그인 제공자 (google, kakao)")
            @PathVariable String provider
    ) {
        // Swagger 문서용 dummy 엔드포인트
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "로그아웃", description = "로그아웃 요청을 처리합니다")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @CookieValue(name = "mm-rt", required = false) String refreshToken,
            HttpServletRequest request,
            HttpServletResponse response) {

        // 쿠키가 있을 때만 DB 삭제
        if (refreshToken != null && !refreshToken.isEmpty()) {
            refreshTokenService.deleteByRefreshToken(refreshToken);
        }

        // 쿠키 삭제
        deleteCookie(response, "mm-rt");
        deleteCookie(response, "JSESSIONID");

        // SecurityContext 클리어
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(Map.of("message", "로그아웃 성공"));
    }

    private void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        // cookie.setSecure(true); // HTTPS 환경에서 사용
    }
}

