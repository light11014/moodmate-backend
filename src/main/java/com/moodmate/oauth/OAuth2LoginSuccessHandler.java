package com.moodmate.oauth;

import com.moodmate.entity.Member;
import com.moodmate.util.JwtUtil;
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

        Member member = userDetails.getMember();

        // JWT 생성
        String token = jwtUtil.createToken(member.getId(), member.getEmail(), member.getRole());


        // JWT를 쿠키에 저장 (또는 localStorage로 프론트에서 저장해도 됨)
        response.setHeader("Authorization", "Bearer " + token);

        // 로그인 성공 후 페이지로 리디렉트
        response.sendRedirect("/login-success.html");

        // 로그 추가
        System.out.println("[DEBUG] Authentication successful : " + member.getEmail());  // 인증 성공 후 로그 추가

        // 닉네임이 아직 없는 경우 → nicknameRequired = true
//        boolean usernameRequired = (member.getUsername() == null || member.getUsername().isBlank());
//        String username = member.getUsername();
//
//        // JSON 응답 작성
//        response.setContentType("application/json");
//        response.setCharacterEncoding("UTF-8");
//
//        String json = """
//        {
//            "token": "%s",
//            "usernameRequired": %s
//            "username": %s
//        }
//        """.formatted(token, usernameRequired, username);
//
//        response.getWriter().write(json);
    }


}

