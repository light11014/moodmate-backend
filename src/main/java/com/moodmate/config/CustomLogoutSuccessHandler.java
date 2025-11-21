package com.moodmate.config;

import com.moodmate.domain.token.service.RefreshTokenService;
import com.moodmate.domain.user.ouath.CustomOauth2User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final RefreshTokenService refreshTokenService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {
        // 쿠키 무효화
        Cookie cookie = new Cookie("mm-rt", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // HTTPS 환경이라면 true
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        // DB에서 RefreshToken 제거
        if (authentication != null && authentication.getPrincipal() instanceof CustomOauth2User user) {
            refreshTokenService.deleteByUserId(user.getUser().getId());
        }

        response.setStatus(HttpServletResponse.SC_OK);
    }
}
