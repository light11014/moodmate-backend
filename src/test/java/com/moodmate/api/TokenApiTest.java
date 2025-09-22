package com.moodmate.api;

import com.moodmate.config.jwt.JwtProperties;
import com.moodmate.config.jwt.JwtTokenProvider;
import com.moodmate.domain.diary.entity.Diary;
import com.moodmate.domain.diary.entity.DiaryEmotion;
import com.moodmate.domain.diary.repository.DiaryEmotionRepository;
import com.moodmate.domain.diary.repository.DiaryRepository;
import com.moodmate.domain.emotion.Emotion;
import com.moodmate.domain.emotion.EmotionRepository;
import com.moodmate.domain.token.RefreshToken;
import com.moodmate.domain.token.RefreshTokenRepository;
import com.moodmate.domain.user.UserRepository;
import com.moodmate.domain.user.entity.User;
import com.moodmate.domain.user.ouath.CustomOauth2User;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
public class TokenApiTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    JwtTokenProvider jwtTokenProvider; // JWT 발급 유틸


    User user;

    String refreshToken;
    String accessToken;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        user = TestUtils.createUser(userRepository);
        refreshToken = TestUtils.createRefreshToken(jwtTokenProvider, user, refreshTokenRepository);
        accessToken = TestUtils.createAccessToken(jwtTokenProvider, user);
    }

    @Test
    void access_token_발급() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("mm-rt", refreshToken)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    public void 로그아웃 () throws Exception {
        // when: 로그아웃 요청
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .cookie(new Cookie("mm-rt", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("mm-rt", 0));
    }

    @Test
    @Transactional
    void 로그아웃시_RefreshToken삭제() throws Exception {
        CustomOauth2User customOauth2User = new CustomOauth2User(user, null);

        // when
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .cookie(new Cookie("mm-rt", refreshToken))
                        .with(oauth2Login().oauth2User(customOauth2User)))
                .andExpect(status().isOk());

        // then
        assertFalse(refreshTokenRepository.findByUserId(user.getId()).isPresent());
    }
}
