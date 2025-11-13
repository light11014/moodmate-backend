package com.moodmate.dev;

import com.moodmate.domain.diary.dto.DiaryRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleDummyDataRequest {
    private List<SimpleUserData> users;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleUserData {
        private Long userId;
        private String username;
        private String email;
        private DiaryRequest diary;
    }
}