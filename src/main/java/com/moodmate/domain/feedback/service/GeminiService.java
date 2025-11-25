package com.moodmate.domain.feedback.service;

import com.moodmate.domain.feedback.entity.FeedbackStyle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    @Qualifier("geminiWebClient")
    private final WebClient geminiWebClient;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.model:gemini-1.5-flash}")
    private String model;

    @Value("${gemini.api.timeout:30}")
    private int timeoutSeconds;

    public String generateSummary(String diaryContent) {
        String prompt = """
                다음 일기를 읽고 주요 사건과 감정을 포함한 40자 이내의 명확하고 짧게 요약을 작성해주세요.
                단순히 '좋은 하루' 같은 짧은 문장이 아니라, 사용자가 기록한 구체적인 내용을 반영해주세요.
                15자 이내의 짧은 일기라면 있는 그대로 출력해주세요.
                            
                일기 내용:
                %s
                """.formatted(diaryContent);

        return callGeminiAPI(prompt, "요약 생성");
    }

    public String generateFeedback(String diaryContent, FeedbackStyle style) {
        String styleDescription = getStyleDescription(style);
        String stylePrompt = getStylePrompt(style);

        String prompt = """
                다음은 사용자가 작성한 일기입니다. %s 스타일로 따뜻하고 진심 어린 피드백을 한국어로 작성해주세요.
                            
                일기 내용:
                %s
                            
                피드백 가이드라인:
                %s
                            
                요구사항:
                - 3-4문장으로 구성
                - 200-300자 내외
                - 사용자의 구체적인 경험이나 감정을 언급
                - 진심 어린 톤으로 작성
                - 한국어로 자연스럽게 작성
                """.formatted(stylePrompt, diaryContent, styleDescription);

        return callGeminiAPI(prompt, "피드백 생성");
    }

    public String generatePeriodSummary(String combinedSummaries, LocalDate startDate, LocalDate endDate) {
        String prompt = """
                다음은 %s부터 %s까지의 일기 요약들입니다.
                이 기간 동안의 전반적인 경험과 상황을 종합하여 4-5문장의 전체 요약을 작성해주세요.
                            
                일기 요약들:
                %s
                            
                요구사항:
                - 이 기간의 주요 테마와 전반적인 분위기 파악
                - 긍정적인 경험과 도전적인 상황을 균형있게 언급
                - 감정의 변화나 패턴이 있다면 언급
                - 따뜻하고 이해심 깊은 톤으로 작성
                - 구체적인 내용을 바탕으로 의미 있는 통찰 제공
                """.formatted(startDate, endDate, combinedSummaries);

        return callGeminiAPI(prompt, "기간별 요약 생성");
    }

    public String generateRecommendations(String combinedSummaries) {
        String prompt = """
                다음 일기 요약들을 바탕으로 사용자에게 도움이 될 수 있는 구체적이고 실용적인 권장사항을 제안해주세요.
                            
                일기 요약들:
                %s
                            
                권장사항 요청:
                - 관찰된 패턴을 바탕으로 한 맞춤형 조언
                - 긍정적인 경험을 더 늘릴 수 있는 방법
                - 스트레스나 어려운 감정 상황에 더 잘 대처하는 방법
                - 성장과 발전을 위한 구체적인 실천 방안
                - 일상에서 쉽게 적용할 수 있는 현실적인 제안
                - 2문장으로 무조건 간단하게 작성
                """.formatted(combinedSummaries);

        return callGeminiAPI(prompt, "권장사항 생성");
    }

    private String callGeminiAPI(String prompt, String operation) {
        try {
            log.info("Gemini API 호출 시작 - 작업: {}, 모델: {}", operation, model);

            if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
                throw new RuntimeException("Gemini API 키가 설정되지 않았습니다");
            }

            Map<String, Object> requestBody = createRequestBody(prompt);

            Map<String, Object> response = geminiWebClient
                    .post()
                    .uri("/v1beta/models/" + model + ":generateContent?key=" + geminiApiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)

                    // 503, 429 같은 과부하 오류 자동 재시도
                    .retryWhen(
                            reactor.util.retry.Retry
                                    .backoff(3, Duration.ofMillis(500)) // 0.5초 → 1초 → 2초
                                    .filter(ex ->
                                            ex instanceof WebClientResponseException &&
                                                    (
                                                            ((WebClientResponseException) ex).getStatusCode().value() == 503 ||
                                                                    ((WebClientResponseException) ex).getStatusCode().value() == 429
                                                    )
                                    )
                    )

                    // 전체 타임아웃
                    .timeout(Duration.ofSeconds(timeoutSeconds))

                    .block();

            log.info("Gemini API 응답 성공 - 작업: {}", operation);
            return parseGeminiResponse(response, operation);

        } catch (WebClientResponseException e) {
            log.error("Gemini API 응답 오류 - 작업: {}, 상태: {}, 응답: {}",
                    operation, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Gemini API 호출 실패: " + e.getStatusCode() + " - " + e.getMessage(), e);

        } catch (Exception e) {
            log.error("Gemini API 호출 중 오류 - 작업: {}, 오류: {}", operation, e.getMessage(), e);
            throw new RuntimeException("Gemini API 호출 중 오류 발생: " + e.getMessage(), e);
        }
    }

    private String parseGeminiResponse(Map<String, Object> response, String operation) {
        try {
            List<?> candidates = (List<?>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                throw new RuntimeException("Gemini API 응답에 candidates가 없습니다");
            }

            Map<?, ?> candidate = (Map<?, ?>) candidates.get(0);
            Map<?, ?> content = (Map<?, ?>) candidate.get("content");
            if (content == null) {
                throw new RuntimeException("Gemini API 응답에 content가 없습니다");
            }

            List<?> parts = (List<?>) content.get("parts");
            if (parts == null || parts.isEmpty()) {
                throw new RuntimeException("Gemini API 응답에 parts가 없습니다");
            }

            Map<?, ?> part = (Map<?, ?>) parts.get(0);
            String text = (String) part.get("text");

            if (text == null || text.trim().isEmpty()) {
                throw new RuntimeException("Gemini API 응답 텍스트가 비어있습니다");
            }

            return text.trim();

        } catch (Exception e) {
            log.error("Gemini 응답 파싱 실패 - 작업: {}, 오류: {}", operation, e.getMessage(), e);
            throw new RuntimeException("Gemini API 응답 파싱 실패: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> createRequestBody(String prompt) {
        return Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "topK", 40,
                        "topP", 0.95,
                        "maxOutputTokens", 4096,
                        "stopSequences", List.of()
                ),
                "safetySettings", List.of(
                        Map.of("category", "HARM_CATEGORY_HARASSMENT", "threshold", "BLOCK_MEDIUM_AND_ABOVE"),
                        Map.of("category", "HARM_CATEGORY_HATE_SPEECH", "threshold", "BLOCK_MEDIUM_AND_ABOVE"),
                        Map.of("category", "HARM_CATEGORY_SEXUALLY_EXPLICIT", "threshold", "BLOCK_MEDIUM_AND_ABOVE"),
                        Map.of("category", "HARM_CATEGORY_DANGEROUS_CONTENT", "threshold", "BLOCK_MEDIUM_AND_ABOVE")
                )
        );
    }

    private String getStylePrompt(FeedbackStyle style) {
        return switch (style) {
            case COMFORT -> "위로와 공감";
            case PRAISE -> "칭찬과 격려";
            case DIRECT -> "솔직하고 직설적";
            case ENCOURAGING -> "응원과 동기부여";
        };
    }

    private String getStyleDescription(FeedbackStyle style) {
        return switch (style) {
            case COMFORT -> "사용자의 감정을 공감하고 위로하는 따뜻한 톤으로 작성해주세요. 힘든 마음을 이해한다는 것을 표현하고, 괜찮아질 거라는 희망을 전해주세요.";
            case PRAISE -> "사용자의 긍정적인 면을 찾아서 칭찬해주세요. 작은 노력이나 성장도 인정해주고, 자존감을 높여주는 내용으로 작성해주세요.";
            case DIRECT -> "솔직하고 현실적인 관점에서 피드백을 주세요. 감정에 치우치지 않고 객관적이면서도 도움이 되는 조언을 해주세요.";
            case ENCOURAGING -> "사용자가 앞으로 더 나아갈 수 있도록 동기를 부여하는 내용으로 작성해주세요. 가능성과 잠재력을 강조하며 응원해주세요.";
        };
    }
}