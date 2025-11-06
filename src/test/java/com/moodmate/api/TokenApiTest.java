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
import com.moodmate.domain.token.TokenType;
import com.moodmate.domain.user.UserRepository;
import com.moodmate.domain.user.entity.User;
import com.moodmate.domain.user.ouath.CustomOauth2User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
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

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Date;
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

    @Autowired
    JwtProperties jwtProperties;
    User user;

    String refreshToken;
    String accessToken;

    @Autowired
    private TestUtils testUtils;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        user = testUtils.createUser(userRepository);
        refreshToken = TestUtils.createRefreshToken(jwtTokenProvider, user, refreshTokenRepository);
        accessToken = TestUtils.createAccessToken(jwtTokenProvider, user);
    }

    @Test
    void access_token_발급() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("mm-rt", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void 만료된_AccessToken_요청_시_401() throws Exception {
        // given: 매우 짧은 만료 시간으로 토큰 생성
        String expiredAccessToken = jwtTokenProvider.generateExpiredToken(user.getId(), TokenType.ACCESS);

        // when & then
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + expiredAccessToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AccessToken expired."));
    }

    @Test
    public void 로그아웃() throws Exception {
        // when: 로그아웃 요청
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .cookie(new Cookie("mm-rt", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("mm-rt", 0));
    }

    @Test
    void 로그아웃시_RefreshToken_삭제() throws Exception {
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

    @Test
    void 만료된_RefreshToken_요청_시_401() throws Exception {
        refreshTokenRepository.deleteAll();
        // given: 매우 짧은 만료 시간으로 토큰 생성
        String expiredRefreshToken = jwtTokenProvider.generateExpiredToken(
                user.getId(),
                TokenType.REFRESH
        );
        refreshTokenRepository.save(new RefreshToken(user, expiredRefreshToken));

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("mm-rt", expiredRefreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("RefreshToken expired. Please login again."));

        // 삭제 확인
        assertFalse(refreshTokenRepository.findByUserId(user.getId()).isPresent());
    }
    @Test
    public void 잘못된_토큰_요청_401_Refresh_대신_Access() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("mm-rt", accessToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Wrong token type: expected REFRESH"));
    }

    @Test
    public void 잘못된_토큰_요청_401_Refresh_토큰_없음() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void 잘못된_토큰_요청_401_Access_대신_Refresh() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Wrong token type: expected ACCESS"));
    }

    @Test
    public void 잘못된_토큰_요청_401_Access_파싱_불가() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer this.is.not.a.jwt"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    @Test
    public void 잘못된_토큰_요청_401_Refresh_파싱_불가() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("mm-rt", "this.is.not.a.jwt")))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    @Test
    public void 잘못된_토큰_요청_401_DB에_없는_Refresh() throws Exception {
        refreshTokenRepository.deleteAll();

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("mm-rt", refreshToken)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("RefreshToken not found"));
    }

}
