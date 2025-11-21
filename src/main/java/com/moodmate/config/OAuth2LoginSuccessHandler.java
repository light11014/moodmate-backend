package com.moodmate.config;

import com.moodmate.config.jwt.JwtTokenProvider;
import com.moodmate.domain.token.service.RefreshTokenService;
import com.moodmate.domain.user.entity.User;
import com.moodmate.domain.user.ouath.CustomOauth2User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // 사용자 정보 꺼내기
        CustomOauth2User userDetails = (CustomOauth2User) authentication.getPrincipal();
        User user = userDetails.getUser();

        // JWT 생성
        String token = jwtTokenProvider.createRefreshToken(user.getId());
        refreshTokenService.saveRefreshToken(user, token);


        // 쿠키 생성
        Cookie cookie = new Cookie("mm-rt", token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setDomain("moodmate.duckdns.org");

        // SameSite 는 Cookie API가 지원 안 하므로 직접 헤더로 설정

        String cookieHeader = String.format(
                "mm-rt=%s; Path=/; HttpOnly; Secure; SameSite=None; Domain=moodmate.duckdns.org",
                token
        );
        response.addHeader("Set-Cookie", cookieHeader);


        // OAuth 후 SPA로 리다이렉트
        response.sendRedirect("https://moodmate.duckdns.org/redirect");

    }
}