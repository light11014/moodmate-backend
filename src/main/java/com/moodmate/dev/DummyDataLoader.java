package com.moodmate.dev;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodmate.config.encryption.EncryptionKeyService;
import com.moodmate.config.encryption.EncryptionService;
import com.moodmate.domain.diary.DiaryService;
import com.moodmate.domain.diary.dto.DiaryRequest;
import com.moodmate.domain.diary.dto.EmotionDto;
import com.moodmate.domain.diary.entity.Diary;
import com.moodmate.domain.diary.repository.DiaryRepository;
import com.moodmate.domain.emotion.Emotion;
import com.moodmate.domain.emotion.EmotionRepository;
import com.moodmate.domain.user.UserRepository;
import com.moodmate.domain.user.entity.Role;
import com.moodmate.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DummyDataLoader implements ApplicationRunner {

    private final UserRepository userRepository;
    private final DiaryRepository diaryRepository;
    private final EncryptionService encryptionService;
    private final EncryptionKeyService encryptionKeyService;

    private final DiaryService diaryService;
    private final ObjectMapper objectMapper;

    private final EmotionRepository emotionRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {

        // --- Emotion 초기화 ---
        List<String> defaultEmotions = List.of("기쁨", "슬픔", "분노", "우울", "뿌듯", "놀람");

        for (String name : defaultEmotions) {
            emotionRepository.findByName(name)
                    .orElseGet(() -> emotionRepository.save(new Emotion(name)));
        }

        // 이미 데이터 있으면 스킵
        if (diaryRepository.count() > 0) {
            log.info("더미 데이터가 이미 존재합니다. 스킵합니다.");
            return;
        }

        log.info("더미 데이터 로딩 시작...");

//        // 1. 상세 사용자 3명 로드
//        loadDetailedUsers();
//
//        // 2. 단순 사용자 97명 로드
//        loadSimpleUsers();
//
//        loadDiariesForCurrentUser();
//
//        log.info("더미 데이터 로딩 완료!");
    }

    private void loadDiariesForCurrentUser() throws Exception {
        ClassPathResource resource = new ClassPathResource("dummy-data/detailed-users.json");
        DummyDataRequest data = objectMapper.readValue(
                resource.getInputStream(),
                DummyDataRequest.class
        );

        DummyDataRequest.DetailedUserData firstUserData = data.getUsers().get(0);

        String targetEmail = "mail@gmail.com"; // 실제 이메일로 변경
        User currentUser = userRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new IllegalStateException("해당 이메일의 사용자를 찾을 수 없습니다: " + targetEmail));

        log.info("현재 사용자: {} (ID: {})", currentUser.getUsername(), currentUser.getId());

        diaryService.createDiariesBatch(currentUser.getId(), firstUserData.getDiaries());

        log.info("일기 {}개 저장 완료!", firstUserData.getDiaries().size());
    }

    private void loadDetailedUsers() throws Exception {
        ClassPathResource resource = new ClassPathResource("dummy-data/detailed-users.json");
        DummyDataRequest data = objectMapper.readValue(
                resource.getInputStream(),
                DummyDataRequest.class
        );

        for (DummyDataRequest.DetailedUserData userData : data.getUsers()) {
            // DEK 생성 및 암호화
            String encryptedDek = encryptionKeyService.createAndEncryptDek();

            // OAuth 사용자 생성
            User user = User.createOAuthUser(
                    "dummy_" + userData.getUserId(),
                    "dummy",
                    String.valueOf(userData.getUserId()),
                    Role.USER,
                    null,
                    userData.getEmail(),
                    encryptedDek
            );
            user.setUsername(userData.getUsername());

            User savedUser = userRepository.save(user);
            log.info("사용자 생성: {} (ID: {})", userData.getUsername(), savedUser.getId());

            // 배치로 일기 생성 (365개 한번에)
            diaryService.createDiariesBatch(savedUser.getId(), userData.getDiaries());

            log.info("  - {} 총 {}개 일기 생성 완료", userData.getUsername(), userData.getDiaries().size());
        }
    }

    private void loadSimpleUsers() throws Exception {
        ClassPathResource resource = new ClassPathResource("dummy-data/simple-users.json");
        SimpleDummyDataRequest data = objectMapper.readValue(
                resource.getInputStream(),
                SimpleDummyDataRequest.class
        );

        List<User> users = new ArrayList<>();

        for (SimpleDummyDataRequest.SimpleUserData userData : data.getUsers()) {
            String encryptedDek = encryptionKeyService.createAndEncryptDek();

            User user = User.createOAuthUser(
                    "dummy_" + userData.getUserId(),
                    "dummy",
                    String.valueOf(userData.getUserId()),
                    Role.USER,
                    null,
                    userData.getEmail(),
                    encryptedDek
            );
            user.setUsername(userData.getUsername());
            users.add(user);

            // 배치 저장
            if (users.size() >= 50) {
                userRepository.saveAll(users);
                users.clear();
            }
        }

        // 남은 사용자 저장
        if (!users.isEmpty()) {
            userRepository.saveAll(users);
        }

        log.info("단순 사용자 {}명 생성 완료", data.getUsers().size());

        // 일기 생성 (각각 1개씩)
        for (SimpleDummyDataRequest.SimpleUserData userData : data.getUsers()) {
            User user = userRepository.findByLoginId("dummy_" + userData.getUserId())
                    .orElseThrow();

            diaryService.saveDiary(user.getId(), userData.getDiary());
        }

        log.info("단순 사용자 일기 생성 완료");
    }
}