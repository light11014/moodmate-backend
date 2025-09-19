package com.moodmate.api;

import com.moodmate.common.JwtUtil;
import com.moodmate.domain.diary.entity.Diary;
import com.moodmate.domain.diary.entity.DiaryEmotion;
import com.moodmate.domain.emotion.Emotion;
import com.moodmate.domain.emotion.EmotionRepository;
import com.moodmate.domain.emotion.EmotionService;
import com.moodmate.domain.emotion.dto.EmotionRequest;
import com.moodmate.domain.emotion.dto.EmotionResponse;
import com.moodmate.domain.user.UserRepository;
import com.moodmate.domain.user.entity.User;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class EmotionApiTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmotionRepository emotionRepository;

    @Autowired
    JwtUtil jwtTokenProvider; // JWT 발급 유틸

    private String token;

    User user;

    @BeforeEach
    void setUp() {
        emotionRepository.deleteAll();
        userRepository.deleteAll();

        // User 생성
        user = TestUtils.createUser(userRepository);

        // 실제 JWT 토큰 발급
        token = TestUtils.createToken(jwtTokenProvider, user);
    }

    @Test
    public void 감정_등록_성공 () throws Exception{
        mockMvc.perform(post("/api/emotions")
                        .cookie(new Cookie("jwt_token", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"기쁨\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("기쁨"));
    }

    @Test
    public void 중복_감정_등록_실패 () throws Exception{
        // given
        emotionRepository.save(new Emotion("기쁨"));

        // when & then
        mockMvc.perform(post("/api/emotions")
                        .cookie(new Cookie("jwt_token", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"기쁨\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("이미 등록된 감정입니다."));
    }

    @Test
    public void 감정_목록_조회 () throws Exception{
        // given
        emotionRepository.save(new Emotion("기쁨"));
        emotionRepository.save(new Emotion("슬픔"));
        emotionRepository.save(new Emotion("우울"));

        // when & then
        mockMvc.perform(get("/api/emotions")
                        .cookie(new Cookie("jwt_token", token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }
}
