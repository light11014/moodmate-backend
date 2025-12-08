package com.moodmate.dev;

import lombok.Data;
import java.util.List;

@Data
public class SeedUserData {
    private List<SeedUser> users;
}

@Data
class SeedUser {
    private Long userId;
    private String username;
    private String email;
    private List<SeedDiary> diaries;
}

@Data
class SeedDiary {
    private String date;
    private String content;
    private List<SeedEmotion> emotions;
}

@Data
class SeedEmotion {
    private String name;
    private int intensity;
}
