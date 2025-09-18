package com.moodmate.api;

import com.moodmate.common.JwtUtil;
import com.moodmate.domain.diary.entity.Diary;
import com.moodmate.domain.diary.entity.DiaryEmotion;
import com.moodmate.domain.diary.repository.DiaryEmotionRepository;
import com.moodmate.domain.diary.repository.DiaryRepository;
import com.moodmate.domain.emotion.Emotion;
import com.moodmate.domain.emotion.EmotionRepository;
import com.moodmate.domain.user.UserRepository;
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

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
public class TrackingApiTest {

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
        Emotion surprised = emotionRepository.save(new Emotion("놀람"));
        Emotion angry = emotionRepository.save(new Emotion("분노"));
        Emotion proud = emotionRepository.save(new Emotion("뿌듯"));
        Emotion depressed = emotionRepository.save(new Emotion("우울"));
        emotionRepository.saveAll(List.of(joy, sad, surprised, angry, proud, depressed));

        // Diary 생성 + DiaryEmotion 연결
        Diary diary1 = Diary.builder()
                .content("오늘은 즐거운 하루였다")
                .date(LocalDate.of(2025, 9, 1))
                .user(user)
                .build();

        diary1.addDiaryEmotion(new DiaryEmotion(joy, 5));
        diary1.addDiaryEmotion(new DiaryEmotion(sad, 5));

        Diary diary2 = Diary.builder()
                .content("오늘은 조금 힘들었다")
                .date(LocalDate.of(2025, 9, 2))
                .user(user)
                .build();

        diary2.addDiaryEmotion(new DiaryEmotion(sad, 5));
        diary2.addDiaryEmotion(new DiaryEmotion(depressed, 5));

        Diary diary3 = Diary.builder()
                .content("오늘은 즐거운 하루였다")
                .date(LocalDate.of(2025, 10, 1))
                .user(user)
                .build();

        diary3.addDiaryEmotion(new DiaryEmotion(joy, 4));
        diary3.addDiaryEmotion(new DiaryEmotion(proud, 4));

        Diary diary4 = Diary.builder()
                .content("오늘은 즐거운 하루였다")
                .date(LocalDate.of(2025, 8, 1))
                .user(user)
                .build();

        diary4.addDiaryEmotion(new DiaryEmotion(surprised, 4));
        diary4.addDiaryEmotion(new DiaryEmotion(sad, 4));

        diaryRepository.save(diary1);
        diaryRepository.save(diary2);
        diaryRepository.save(diary3);
        diaryRepository.save(diary4);
    }

    @Test
    void 감정빈도_API가_JSON으로_응답() throws Exception {
        mockMvc.perform(get("/api/analytics/emotions/frequency")
                        .cookie(new Cookie("jwt_token", token))
                        .param("startDate", "2025-09-01")
                        .param("endDate", "2025-09-30"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.totalRecords").value(3))
                .andExpect(jsonPath("$.data[?(@.emotion == '기쁨')].count").value(1))
                .andExpect(jsonPath("$.data[?(@.emotion == '슬픔')].count").value(2));
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
                .andExpect(jsonPath("$.meta.totalRecords").value(3))
                .andExpect(jsonPath("$.data[?(@.emotion == '기쁨')].ratio").value(0.25))
                .andExpect(jsonPath("$.data[?(@.emotion == '슬픔')].ratio").value(0.5));
    }

    @Test
    void 감정_추세_API가_JSON으로_응답() throws Exception {
        mockMvc.perform(get("/api/analytics/emotions/trend")
                        .cookie(new Cookie("jwt_token", token))
                        .param("startDate", "2025-08-01")
                        .param("endDate", "2025-09-30")
                        .param("emotions", "슬픔"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.selectedEmotions", containsInAnyOrder("슬픔")))
                .andExpect(jsonPath("$.data[*].emotion", containsInAnyOrder("슬픔")))
                .andExpect(jsonPath("$.data[?(@.emotion == '슬픔')].timeline[0].date").value("2025-08-01"))
                .andExpect(jsonPath("$.data[?(@.emotion == '슬픔')].timeline[0].intensity").value(4));
    }

    @Test
    void 감정_추세_API_공백조회_응답() throws Exception {
        mockMvc.perform(get("/api/analytics/emotions/trend")
                        .cookie(new Cookie("jwt_token", token))
                        .param("startDate", "2025-08-01")
                        .param("endDate", "2025-09-30"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.selectedEmotions", hasSize(6)))
                .andExpect(jsonPath("$.data[?(@.emotion == '슬픔')].timeline[0].date").value("2025-08-01"))
                .andExpect(jsonPath("$.data[?(@.emotion == '슬픔')].timeline[0].intensity").value(4));
    }
}