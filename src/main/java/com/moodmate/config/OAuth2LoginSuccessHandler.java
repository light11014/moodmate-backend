package com.moodmate.config;

import com.moodmate.common.JwtUtil;
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

    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // 사용자 정보 꺼내기
        CustomOauth2User userDetails = (CustomOauth2User) authentication.getPrincipal();
        User user = userDetails.getUser();

        // JWT 생성
        String token = jwtUtil.createToken(user.getId(), user.getEmail(), user.getRole());

        // 쿠키 생성
        Cookie cookie = new Cookie("jwt_token", token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        // cookie.setSecure(true); // HTTPS 환경에서 사용

        // 쿠키 추가
        response.addCookie(cookie);

        // 리다이렉트
        response.sendRedirect("http://localhost:3000/calendar");
    }
}