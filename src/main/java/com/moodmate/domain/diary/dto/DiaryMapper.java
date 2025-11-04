package com.moodmate.domain.diary.dto;

import com.moodmate.config.encryption.EncryptionUtil;
import com.moodmate.domain.diary.entity.Diary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DiaryMapper {
    private final EncryptionUtil encryptionUtil;

    /**
     * 암호화된 Diary를 복호화하여 Response DTO로 변환
     */
    public DiaryResponse toResponseDto(Diary diary, String dek) {
        try {
            return new DiaryResponse(
                    diary.getId(),
                    encryptionUtil.decrypt(diary.getContent(), dek),
                    diary.getDate(),
                    diary.getDiaryEmotions().stream()
                            .map(e -> new EmotionDto(e.getEmotion().getName(), e.getIntensity()))
                            .toList(),
                    diary.getCreated_at(),
                    diary.getUpdated_at()
            );
        } catch (Exception e) {
            throw new RuntimeException("일기 복호화 중 오류 발생", e);
        }
    }

    /**
     * 여러 일기를 한번에 변환
     */
    public List<DiaryResponse> toResponseDtoList(List<Diary> diaries, String dek) {
        return diaries.stream()
                .map(diary -> toResponseDto(diary, dek))
                .collect(Collectors.toList());
    }
}
