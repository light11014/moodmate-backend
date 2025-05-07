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
        String token = jwtUtil.createToken(member.getId(), member.getRole());

        // 닉네임이 아직 없는 경우 → nicknameRequired = true
        boolean nicknameRequired = (member.getUsername() == null || member.getUsername().isBlank());

        // JSON 응답 작성
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String json = """
        {
            "token": "%s",
            "nicknameRequired": %s
        }
        """.formatted(token, nicknameRequired);

        response.getWriter().write(json);
    }
}

