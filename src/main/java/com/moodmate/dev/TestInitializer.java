package com.moodmate.dev;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodmate.config.encryption.EncryptionKeyService;
import com.moodmate.dev.SeedUser;
import com.moodmate.dev.SeedUserData;
import com.moodmate.domain.diary.DiaryService;
import com.moodmate.domain.diary.dto.DiaryRequest;
import com.moodmate.domain.diary.dto.EmotionDto;
import com.moodmate.domain.diary.entity.Diary;
import com.moodmate.domain.diary.entity.DiaryEmotion;
import com.moodmate.domain.diary.repository.DiaryRepository;
import com.moodmate.domain.emotion.Emotion;
import com.moodmate.domain.emotion.EmotionRepository;
import com.moodmate.domain.user.UserRepository;
import com.moodmate.domain.user.entity.Role;
import com.moodmate.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class TestInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EmotionRepository emotionRepository;
    private final DiaryRepository diaryRepository;  // 일기 저장용
    private final EncryptionKeyService keyService;

    private final DiaryService diaryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String TARGET_EMAIL = "dhtcht1014@gmail.com"; // 너가 원하는 이메일
    private static final String JSON_PATH = "detailed-users.json";

    @Override
    public void run(String... args) throws Exception {

//        // 1. JSON 로드
//        SeedUserData seed = loadJson();
//
//        // 2. JSON의 user1을 가져오지만
//        SeedUser seedUser = seed.getUsers().stream()
//                .filter(u -> u.getUserId() == 1)   // userId로 찾는 방식 추천
//                .findFirst()
//                .orElseThrow();
//
//        // 3. 실제 저장할 이메일은 무조건 내가 지정한 이메일로 덮어쓰기
//        String emailToUse = TARGET_EMAIL;
//
//        // 4. DB에서 찾거나 생성
//        User user = userRepository.findByEmail(emailToUse)
//                .orElseGet(() -> {
//                    User newUser = null;
//                    try {
//                        newUser = User.createOAuthUser(
//                                seedUser.getUsername(),   // JSON의 username 사용
//                                seedUser.getUsername(),
//                                "001",
//                                Role.USER,
//                                emailToUse,               // ⭐ 여기! JSON 이메일 대신 TARGET_EMAIL
//                                "test",
//                                keyService.createAndEncryptDek()
//                        );
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                    return userRepository.save(newUser);
//                });
//
//        // 4. Emotion 기본값 등록
//        initDefaultEmotions();
//
//        // 5. Diary 저장
//        for (SeedDiary d : seedUser.getDiaries()) {
//
//            DiaryRequest req = new DiaryRequest(
//                    d.getContent(),
//                    LocalDate.parse(d.getDate()),
//                    d.getEmotions().stream()
//                            .map(e -> new EmotionDto(e.getName(), e.getIntensity()))
//                            .toList()
//            );
//
//            diaryService.saveDiary(user.getId(), req);  // ⭐ 서비스 태우기 → 암호화 적용
//        }

    }

    private SeedUserData loadJson() throws IOException, FileNotFoundException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(JSON_PATH);
        if (is == null) {
            throw new FileNotFoundException("JSON not found: " + JSON_PATH);
        }
        return objectMapper.readValue(is, SeedUserData.class);
    }

    private void initDefaultEmotions() {
        List<String> defaultEmotions = List.of("기쁨", "슬픔", "분노", "우울", "뿌듯", "놀람");
        defaultEmotions.forEach(name ->
                emotionRepository.findByName(name)
                        .orElseGet(() -> emotionRepository.save(new Emotion(name)))
        );
    }

    private void saveDiaries(SeedUser seedUser, User user) {
        seedUser.getDiaries().forEach(d -> {
            Diary diary = new Diary(d.getContent(), LocalDate.parse(d.getDate()), user);

            diaryRepository.save(diary);

            d.getEmotions().forEach(e -> {
                Emotion emotion = emotionRepository.findByName(e.getName())
                        .orElseThrow(() -> new RuntimeException("Emotion not found: " + e.getName()));

                diary.addDiaryEmotion(new DiaryEmotion(emotion, e.getIntensity()));
            });

            diaryRepository.save(diary);
        });
    }
}
