package com.moodmate.dev;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodmate.config.encryption.EncryptionKeyService;
import com.moodmate.domain.diary.dto.DiaryRequest;
import com.moodmate.domain.diary.dto.EmotionDto;
import com.moodmate.domain.diary.service.DiaryService;
import com.moodmate.domain.emotion.entity.Emotion;
import com.moodmate.domain.emotion.repository.EmotionRepository;
import com.moodmate.domain.user.repository.UserRepository;
import com.moodmate.domain.user.entity.Role;
import com.moodmate.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class TestInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final DiaryService diaryService; // DiaryService 추가 필요


    @Override
    public void run(String... args) throws Exception {
        // 설정값
        String targetUserEmail = "moodmate038@gmail.com"; // 데이터를 넣을 사용자 이메일
        String startDate = "2025-08-15"; // 시작 날짜
        String jsonFilePath = "classpath:diary_dummy_30.json"; // JSON 파일 경로

        insertDummyData(targetUserEmail, startDate, jsonFilePath);
    }

    private void insertDummyData(String userEmail, String startDate, String jsonFilePath) throws Exception {
        // 1. 사용자 조회
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userEmail));

        // 2. JSON 파일 읽기
        ObjectMapper objectMapper = new ObjectMapper();
        Resource resource = new ClassPathResource(jsonFilePath.replace("classpath:", ""));
        List<DiaryData> diaryDataList = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<List<DiaryData>>() {}
        );

        // 3. 시작 날짜 파싱
        LocalDate currentDate = LocalDate.parse(startDate);

        // 4. 각 데이터를 서비스를 통해 삽입
        for (DiaryData data : diaryDataList) {
            try {
                DiaryRequest request = DiaryRequest.builder()
                        .content(data.getContent())
                        .date(currentDate)
                        .emotions(data.getEmotions().stream()
                                .map(e -> new EmotionDto(e.getName(), e.getIntensity()))
                                .collect(Collectors.toList()))
                        .build();

                diaryService.saveDiary(user.getId(), request);

                System.out.println("데이터 삽입 완료: " + currentDate);
                currentDate = currentDate.plusDays(1); // 다음 날로 이동

            } catch (Exception e) {
                System.err.println("데이터 삽입 실패 (날짜: " + currentDate + "): " + e.getMessage());
            }
        }

        System.out.println("총 " + diaryDataList.size() + "개의 더미 데이터 삽입 완료");
    }

    // JSON 데이터 매핑용 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class DiaryData {
        private String content;
        private String date; // 사용하지 않지만 JSON 파싱을 위해 필요
        private List<EmotionData> emotions;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class EmotionData {
        private String name;
        private Integer intensity;
    }
}