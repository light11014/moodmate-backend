package com.moodmate.api;

import com.moodmate.config.jwt.JwtTokenProvider;
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

    @Autowired
    JwtTokenProvider jwtTokenProvider; // JWT 발급 유틸

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
        token = TestUtils.createAccessToken(jwtTokenProvider, user);

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
                .content("오늘 친구들과 카페에서 만났다. \n" +
                        "정말 오랜만에 보는 얼굴들이라 너무 반가웠다. \n" +
                        "맛있는 커피를 마시면서 이런저런 이야기를 나눴다.\n" +
                        "학교 생활이나 취업 준비 이야기를 하면서 시간 가는 줄 몰랐다.\n" +
                        "행복한 하루였다. 일기")
                .date(LocalDate.of(2025, 9, 1))
                .user(user)
                .build();

        diary1.addDiaryEmotion(new DiaryEmotion(joy, 5));
        diary1.addDiaryEmotion(new DiaryEmotion(sad, 5));

        Diary diary2 = Diary.builder()
                .content("오늘은 면접을 봤다. \n" +
                        "너무 긴장돼서 떨렸다. \n" +
                        "준비한 대로 말하지 못해서 아쉬웠다.\n" +
                        "면접관들 앞에서 제대로 말을 못한 것 같아 불안하다.\n" +
                        "결과가 걱정된다. 일기")
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

        Diary diary5 = Diary.builder()
                .content("드디어 합격 통보를 받았다!\n" +
                        "정말 기쁘고 감사하다.\n" +
                        "부모님께 전화로 말씀드렸더니 너무 좋아하셨다.\n" +
                        "친구들도 축하해줬다.\n" +
                        "새로운 회사에서 열심히 일해야겠다. 일기")
                .date(LocalDate.of(2025, 9, 8)) // 월요일
                .user(user)
                .build();
        diary5.addDiaryEmotion(new DiaryEmotion(joy, 3));
        diary5.addDiaryEmotion(new DiaryEmotion(angry, 2));

        Diary diary6 = Diary.builder()
                .content("첫 출근을 했다.\n" +
                        "새로운 사람들을 만나서 조금 떨렸지만 다들 친절했다.\n" +
                        "업무를 배우는 게 쉽지 않지만 재미있다.\n" +
                        "점심시간에 동료들과 식사하면서 회사 분위기를 느꼈다.\n" +
                        "앞으로가 기대된다. 일기")
                .date(LocalDate.of(2025, 9, 15)) // 월요일
                .user(user)
                .build();
        diary6.addDiaryEmotion(new DiaryEmotion(joy, 4));

        diaryRepository.save(diary1);
        diaryRepository.save(diary2);
        diaryRepository.save(diary3);
        diaryRepository.save(diary4);
        diaryRepository.save(diary5);
        diaryRepository.save(diary6);
    }

    @Test
    void 감정빈도_API가_JSON으로_응답() throws Exception {
        mockMvc.perform(get("/api/analytics/emotions/frequency")
                        .header("Authorization", "Bearer " + token)
                        .param("startDate", "2025-09-01")
                        .param("endDate", "2025-09-30"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.totalRecords").value(4))
                .andExpect(jsonPath("$.data[?(@.emotion == '기쁨')].count").value(3))
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
                        .header("Authorization", "Bearer " + token)
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
                        .header("Authorization", "Bearer " + token)
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
                        .header("Authorization", "Bearer " + token)
                        .param("startDate", "2025-08-01")
                        .param("endDate", "2025-09-30"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.selectedEmotions", hasSize(6)))
                .andExpect(jsonPath("$.data[?(@.emotion == '슬픔')].timeline[0].date").value("2025-08-01"))
                .andExpect(jsonPath("$.data[?(@.emotion == '슬픔')].timeline[0].intensity").value(4));
    }

    @Test
    void 요일별_감정_조회_API가_JSON으로_응답() throws Exception {
        mockMvc.perform(get("/api/analytics/emotions/day-of-week")
                        .header("Authorization", "Bearer " + token)
                        .param("startDate", "2025-09-01")
                        .param("endDate", "2025-09-30"))
                .andDo(print())
                .andExpect(status().isOk())
                // 메타데이터 검증
                .andExpect(jsonPath("$.meta.userId").value(user.getId()))
                .andExpect(jsonPath("$.meta.startDate").value("2025-09-01"))
                .andExpect(jsonPath("$.meta.endDate").value("2025-09-30"))
                .andExpect(jsonPath("$.meta.totalRecords").value(7))
                .andExpect(jsonPath("$.meta.totalDiaryCount").value(4))
                // 월요일 검증
                .andExpect(jsonPath("$.data[0].dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$.data[0].dayOfWeekKo").value("월요일"))
                .andExpect(jsonPath("$.data[0].diaryCount").value(3))
                .andExpect(jsonPath("$.data[0].emotionCount").value(3))
                .andExpect(jsonPath("$.data[0].emotions", hasSize(3)))
                // 화요일 검증
                .andExpect(jsonPath("$.data[1].dayOfWeek").value("TUESDAY"))
                .andExpect(jsonPath("$.data[1].dayOfWeekKo").value("화요일"))
                .andExpect(jsonPath("$.data[1].diaryCount").value(1))
                .andExpect(jsonPath("$.data[1].emotionCount").value(2))
                .andExpect(jsonPath("$.data[1].emotions", hasSize(2)))
                // 금요일 검증
                .andExpect(jsonPath("$.data[4].dayOfWeek").value("FRIDAY"))
                .andExpect(jsonPath("$.data[4].dayOfWeekKo").value("금요일"))
                .andExpect(jsonPath("$.data[4].diaryCount").value(0))
                .andExpect(jsonPath("$.data[4].emotionCount").value(0))
                .andExpect(jsonPath("$.data[4].emotions", hasSize(0)));
    }

    @Test
    void 자주_사용한_단어_조회_API() throws Exception {
        mockMvc.perform(get("/api/analytics/words/frequency")
                        .header("Authorization", "Bearer " + token)
                        .param("startDate", "2025-09-01")
                        .param("endDate", "2025-09-30")
                        .param("limit", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.userId").value(user.getId()))
                .andExpect(jsonPath("$.meta.diaryCount").value(4))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].word").exists())
                .andExpect(jsonPath("$.data[0].count").exists());
    }
}