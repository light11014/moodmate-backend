package com.moodmate.dev;

import com.moodmate.domain.diary.dto.DiaryRequest;
import lombok.Data;

import java.util.List;

@Data
public class DummyDataRequest {
    private List<DetailedUserData> users;

    @Data
    public static class DetailedUserData {
        private Long userId;
        private String username;
        private String email;
        private List<DiaryRequest> diaries;
    }

    @Data
    public static class EmotionData {
        private String emotion;
        private Integer intensity;
    }
}

