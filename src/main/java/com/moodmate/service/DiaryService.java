package com.moodmate.service;

import com.moodmate.dto.DiarySaveRequestDto;
import com.moodmate.entity.Diary;
import com.moodmate.entity.DiaryEmotion;
import com.moodmate.entity.Emotion;
import com.moodmate.entity.User;
import com.moodmate.repository.DiaryRepository;
import com.moodmate.repository.EmotionRepository;
import com.moodmate.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final EmotionRepository emotionRepository;
    private final UserRepository userRepository;

    public Long saveDiary(DiarySaveRequestDto dto, Long userId) {
        // 1. 작성자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Diary 생성
        Diary diary = new Diary(dto.getContent(), dto.getDate(), user);

        // 3. 감정 리스트 처리
        for (DiarySaveRequestDto.EmotionRequest e : dto.getEmotions()) {
            Emotion emotion = emotionRepository.findByName(e.getName())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 감정입니다: " + e.getName()));

            DiaryEmotion diaryEmotion = new DiaryEmotion(emotion, e.getIntensity());
            diary.addDiaryEmotion(diaryEmotion); // 양방향 연결
        }

        // 4. 저장 (Cascade로 DiaryEmotion까지 저장됨)
        diaryRepository.save(diary);

        return diary.getId();
    }
}
