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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserApiTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    DiaryRepository diaryRepository;
    @Autowired
    EmotionRepository emotionRepository;
    @Autowired
    DiaryEmotionRepository diaryEmotionRepository;

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

        // Diary 생성 + DiaryEmotion 연결
        Diary diary1 = Diary.builder()
                .content("오늘은 즐거운 하루였다")
                .date(LocalDate.of(2025, 9, 1))
                .user(user)
                .build();

        diary1.addDiaryEmotion(new DiaryEmotion(joy, 5));
        diary1.addDiaryEmotion(new DiaryEmotion(sad, 5));

        diaryRepository.save(diary1);
    }
    @Test
    public void 회원_삭제_일기_연관_있어도_삭제된다() throws Exception {
        // given
        Long userId = user.getId(); // setUp에서 만든 user

        // when
        mockMvc.perform(delete("/api/users/me")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());

        // then
        assertThat(userRepository.existsById(userId)).isFalse();
        assertThat(diaryRepository.findAll()).isEmpty();
        assertThat(diaryEmotionRepository.findAll()).isEmpty();
    }
}
