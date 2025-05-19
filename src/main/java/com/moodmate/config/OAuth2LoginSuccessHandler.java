package com.moodmate.config;

import com.moodmate.domain.user.entity.User;
import com.moodmate.common.JwtUtil;
import com.moodmate.domain.user.ouath.CustomOauth2User;
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

        // 로그 추가
        // System.out.println("[DEBUG] Authentication successful : " + user.getEmail());

        // 닉네임 유무 확인
        boolean usernameRequired = (user.getUsername() == null || user.getUsername().isBlank());
        String username = user.getUsername();

        String json = """
        {
            "token": "%s",
            "usernameRequired": %s
            "username": %s
        }
        """.formatted(token, usernameRequired, username);

        // JSON 응답 작성
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(json);
    }
}

