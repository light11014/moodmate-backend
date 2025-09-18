package com.moodmate.domain.tracking;

import com.moodmate.domain.diary.repository.DiaryEmotionRepository;
import com.moodmate.domain.emotion.EmotionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class TrackingServiceTest {

    private TrackingService trackingService;
    private DiaryEmotionRepository diaryEmotionRepository;

    private EmotionRepository emotionRepository;

    @BeforeEach
    void setUp() {
        // Repository는 mock 처리
        diaryEmotionRepository = mock(DiaryEmotionRepository.class);
        emotionRepository = mock(EmotionRepository.class);
        trackingService = new TrackingService(diaryEmotionRepository, emotionRepository);
    }

    @Test
    @DisplayName("startDate가 endDate보다 뒤면 IllegalArgumentException 발생")
    void startDate가_endDate보다_뒤면_예외() {
        // given
        Long userId = 1L;
        LocalDate startDate = LocalDate.of(2025, 9, 10);
        LocalDate endDate = LocalDate.of(2025, 9, 1);

        // when & then
        assertThatThrownBy(() ->
                trackingService.getEmotionFrequency(userId, startDate, endDate)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("기간 설정이 올바르지 않습니다");
    }

    @Test
    @DisplayName("userId가 null이면 IllegalArgumentException 발생")
    void userId가_null이면_예외() {
        // given
        LocalDate startDate = LocalDate.of(2025, 9, 1);
        LocalDate endDate = LocalDate.of(2025, 9, 5);

        // when & then
        assertThatThrownBy(() ->
                trackingService.getEmotionFrequency(null, startDate, endDate)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("필수 파라미터를 입력해주세요");
    }
}