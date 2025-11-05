package com.moodmate.domain.feedback.dto;

import com.moodmate.config.encryption.EncryptionService;
import com.moodmate.domain.feedback.entity.AiFeedback;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FeedbackMapper {
    private final EncryptionService encryptionService;

    /**
     * 암호화된 Feedback를 복호화하여 Response DTO로 변환
     */
    public FeedbackResponse toResponseDto(AiFeedback feedback, String dek) {
        try {
            return new FeedbackResponse(
                    feedback.getId(),
                    feedback.getDiary().getId(),
                    feedback.getDiary().getDate(),
                    encryptionService.decrypt(feedback.getSummary(), dek),
                    encryptionService.decrypt(feedback.getResponse(), dek),
                    feedback.getFeedbackStyle(),
                    feedback.getRequestedAt(),
                    feedback.getCreated_at()
            );
        } catch (Exception e) {
            throw new RuntimeException("AI 피드백 복호화 중 오류 발생", e);
        }
    }

    /**
     * 여러 피드백를 한번에 변환
     */
    public FeedbackHistoryResponse toFeedbackHistoryResponse(
            List<AiFeedback> aiFeedbacks,
            String dek,
            LocalDate startDate,
            LocalDate endDate) {

        List<FeedbackResponse> items = aiFeedbacks.stream()
                .map(feedback -> toResponseDto(feedback, dek))
                .toList();

        return new FeedbackHistoryResponse(startDate, endDate, items);
    }
}

