package com.moodmate.domain.feedback.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.moodmate.domain.feedback.entity.AiFeedback;
import com.moodmate.domain.feedback.entity.FeedbackStyle;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Slf4j
@JsonInclude(JsonInclude.Include.ALWAYS) // null 값도 포함하여 JSON에 표시
public class FeedbackHistoryItem {
    @Schema(description = "피드백 ID")
    private final Long feedbackId;

    @Schema(description = "일기 ID")
    private final Long diaryId;

    @Schema(description = "일기 날짜")
    private final LocalDate diaryDate;

    @Schema(description = "일기 요약", example = "오늘은 친구들과 즐거운 시간을 보냈지만 한편으로는 과제에 대한 걱정이 있었다.")
    private final String summary;

    @Schema(description = "AI 피드백 응답", example = "친구들과 함께한 즐거운 시간이 정말 소중하네요! 과제에 대한 걱정이 있으시겠지만, 이렇게 균형 잡힌 시간을 보내시는 것 자체가 대단한 것 같아요.")
    private final String response;

    @Schema(description = "피드백 스타일")
    private final FeedbackStyle feedbackStyle;

    @Schema(description = "피드백 생성 시간")
    private final LocalDateTime createdAt;

    public FeedbackHistoryItem(AiFeedback feedback) {
        this.feedbackId = feedback.getId();
        this.diaryId = feedback.getDiary().getId();
        this.diaryDate = feedback.getDiary().getDate();
        this.summary = feedback.getSummary();
        this.response = feedback.getResponse();
        this.feedbackStyle = feedback.getFeedbackStyle();
        this.createdAt = feedback.getCreated_at();

        // 디버그 로그 추가
        log.debug("FeedbackHistoryItem 생성 - ID: {}, Summary: {}, Response: {}",
                feedback.getId(),
                feedback.getSummary() != null ? "존재" : "null",
                feedback.getResponse() != null ? "존재" : "null");

        if (feedback.getResponse() == null) {
            log.warn("피드백 ID {}의 response가 null입니다.", feedback.getId());
        }
    }
}