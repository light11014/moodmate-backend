package com.moodmate.api;

import com.moodmate.common.JwtUtil;
import com.moodmate.domain.diary.entity.Diary;
import com.moodmate.domain.diary.entity.DiaryEmotion;
import com.moodmate.domain.diary.repository.DiaryEmotionRepository;
import com.moodmate.domain.diary.repository.DiaryRepository;
import com.moodmate.domain.emotion.Emotion;
import com.moodmate.domain.emotion.EmotionRepository;
import com.moodmate.domain.user.UserRepository;
import com.moodmate.domain.user.entity.Role;
import com.moodmate.domain.user.entity.User;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
public class TrackingApiIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired DiaryRepository diaryRepository;
    @Autowired EmotionRepository emotionRepository;
    @Autowired DiaryEmotionRepository diaryEmotionRepository;

    @Autowired JwtUtil jwtTokenProvider; // JWT 발급 유틸

    private String token;

    User user;

    @BeforeEach
    void setUp() {
        diaryEmotionRepository.deleteAll();
        diaryRepository.deleteAll();
        emotionRepository.deleteAll();
        userRepository.deleteAll();

        // User 생성
        user = TestUtils.createUser(userRepository);

        // 실제 JWT 토큰 발급
        token = TestUtils.createToken(jwtTokenProvider, user);

        // Emotion 생성
        Emotion joy = emotionRepository.save(new Emotion("기쁨"));
        Emotion sad = emotionRepository.save(new Emotion("슬픔"));

        // Diary 생성 + DiaryEmotion 연결
        Diary diary1 = Diary.builder()
                .content("오늘은 즐거운 하루였다")
                .date(LocalDate.of(2025, 9, 1))
                .user(user)
                .build();

        Diary diary2 = Diary.builder()
                .content("오늘은 조금 힘들었다")
                .date(LocalDate.of(2025, 9, 2))
                .user(user)
                .build();

        // DiaryEmotion 추가 (addDiaryEmotion 활용)
        diary1.addDiaryEmotion(new DiaryEmotion(joy, 4));
        diary1.addDiaryEmotion(new DiaryEmotion(sad, 3));
        diary2.addDiaryEmotion(new DiaryEmotion(joy, 3));

        diaryRepository.save(diary1);
        diaryRepository.save(diary2);
    }

    @Test
    void 감정빈도_API가_JSON으로_응답() throws Exception {
        mockMvc.perform(get("/api/analytics/emotions/frequency")
                        .cookie(new Cookie("jwt_token", token))
                        .param("startDate", "2025-09-01")
                        .param("endDate", "2025-09-05"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.totalRecords").value(2))
                .andExpect(jsonPath("$.data[0].emotion").value("기쁨"))
                .andExpect(jsonPath("$.data[0].count").value(2))
                .andExpect(jsonPath("$.data[1].emotion").value("슬픔"))
                .andExpect(jsonPath("$.data[1].count").value(1));
    }

    @Test
    void 감정빈도_API_JWT_없을때_401() throws Exception {
        mockMvc.perform(get("/api/analytics/emotions/frequency")
                        .param("startDate", "2025-09-01")
                        .param("endDate", "2025-09-05"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 감정강도비율_API가_JSON으로_응답() throws Exception {
        mockMvc.perform(get("/api/analytics/emotions/ratio")
                        .cookie(new Cookie("jwt_token", token))
                        .param("startDate", "2025-09-01")
                        .param("endDate", "2025-09-05"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.totalRecords").value(2))
                .andExpect(jsonPath("$.data[0].emotion").value("기쁨"))
                .andExpect(jsonPath("$.data[0].ratio").value(0.7))
                .andExpect(jsonPath("$.data[1].emotion").value("슬픔"))
                .andExpect(jsonPath("$.data[1].ratio").value(0.3));
    }
}