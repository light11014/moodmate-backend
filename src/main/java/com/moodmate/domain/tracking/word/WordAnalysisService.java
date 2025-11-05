package com.moodmate.domain.tracking.word;

import com.moodmate.config.encryption.EncryptionKeyService;
import com.moodmate.config.encryption.EncryptionService;
import com.moodmate.domain.diary.entity.Diary;
import com.moodmate.domain.diary.repository.DiaryRepository;
import com.moodmate.domain.tracking.word.dto.WordFrequency;
import com.moodmate.domain.tracking.word.dto.WordFrequencyMeta;
import com.moodmate.domain.tracking.word.dto.WordFrequencyResponse;
import com.moodmate.domain.user.UserRepository;
import com.moodmate.domain.user.entity.User;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WordAnalysisService {

    private final Komoran komoran = new Komoran(DEFAULT_MODEL.LIGHT);
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final EncryptionKeyService encryptionKeyService;

    // 상수: 한국어 불용어
    private static final Set<String> KOREAN_STOP_WORDS = Set.of(
            "것", "수", "등", "및", "제", "약", "전", "후", "간", "중"
    );

    // 상수: 영어 불용어
    private static final Set<String> ENGLISH_STOP_WORDS = Set.of(
            "the", "is", "am", "are", "was", "were", "been", "be",
            "have", "has", "had", "do", "does", "did", "will", "would",
            "should", "could", "may", "might", "must", "can",
            "of", "to", "in", "for", "on", "with", "at", "by", "from",
            "this", "that", "these", "those", "my", "your", "his", "her"
    );

    // 상수: 영어 단어 패턴 (사전 컴파일)
    private static final Pattern ENGLISH_PATTERN = Pattern.compile("[a-zA-Z]{2,}");

    /**
     * 지정된 기간 동안의 일기에서 빈도수가 높은 단어를 분석
     *
     * @param userId    사용자 ID
     * @param startDate 시작 날짜
     * @param endDate   종료 날짜
     * @param limit     반환할 최대 단어 수
     * @return 단어 빈도 분석 결과
     */
    public WordFrequencyResponse getFrequentWords(Long userId, LocalDate startDate, LocalDate endDate, int limit) {
        // 1. 일기 목록 조회
        List<Diary> diaries = diaryRepository.findByUserIdAndDateBetween(userId, startDate, endDate);

        // 2. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 3. DEK 복호화 (한 번만 수행)
        String dek;
        try {
            dek = encryptionKeyService.decryptDek(user.getEncryptedDek());
        } catch (Exception e) {
            log.error("DEK 복호화 실패 - 사용자 ID: {}", userId, e);
            throw new RuntimeException("암호화 키 복호화에 실패했습니다.", e);
        }

        // 4. 단어 빈도 분석
        Map<String, Long> wordFrequency = new HashMap<>();
        long totalWordCount = 0;

        for (Diary diary : diaries) {
            try {
                // 일기 내용 복호화
                String content = encryptionService.decrypt(diary.getContent(), dek);

                // 한국어 단어 분석
                long koreanWordCount = analyzeKoreanWords(content, wordFrequency);
                totalWordCount += koreanWordCount;

                // 영어 단어 분석
                long englishWordCount = analyzeEnglishWords(content, wordFrequency);
                totalWordCount += englishWordCount;

            } catch (Exception e) {
                log.error("일기 분석 중 오류 발생 - 일기 ID: {}, 오류: {}", diary.getId(), e.getMessage(), e);
                // 하나의 일기 분석 실패가 전체 분석을 중단시키지 않도록 continue
            }
        }

        // 5. 상위 N개 단어 추출
        List<WordFrequency> topWords = wordFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> new WordFrequency(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        // 6. 메타 정보 생성
        WordFrequencyMeta meta = new WordFrequencyMeta(
                userId,
                startDate,
                endDate,
                (long) diaries.size(),
                totalWordCount,
                wordFrequency.size(),
                LocalDateTime.now()
        );

        log.info("단어 빈도 분석 완료 - 사용자: {}, 일기 수: {}, 총 단어 수: {}, 고유 단어 수: {}",
                userId, diaries.size(), totalWordCount, wordFrequency.size());

        return new WordFrequencyResponse(meta, topWords);
    }

    /**
     * 한국어 단어 분석 (Komoran 형태소 분석기 사용)
     *
     * @param content       일기 내용
     * @param wordFrequency 단어 빈도 맵
     * @return 추출된 한국어 단어 개수
     */
    private long analyzeKoreanWords(String content, Map<String, Long> wordFrequency) {
        KomoranResult result = komoran.analyze(content);

        return result.getTokenList().stream()
                .filter(this::isMeaningfulToken)
                .map(Token::getMorph)
                .filter(word -> word.length() > 1)
                .filter(word -> !KOREAN_STOP_WORDS.contains(word))
                .peek(word -> wordFrequency.merge(word, 1L, Long::sum))
                .count();
    }

    /**
     * 영어 단어 분석 (정규식 사용)
     *
     * @param content       일기 내용
     * @param wordFrequency 단어 빈도 맵
     * @return 추출된 영어 단어 개수
     */
    private long analyzeEnglishWords(String content, Map<String, Long> wordFrequency) {
        long count = 0;
        Matcher matcher = ENGLISH_PATTERN.matcher(content);

        while (matcher.find()) {
            String word = matcher.group().toLowerCase();
            if (!ENGLISH_STOP_WORDS.contains(word)) {
                wordFrequency.merge(word, 1L, Long::sum);
                count++;
            }
        }

        return count;
    }

    /**
     * 의미 있는 품사인지 확인
     *
     * @param token 형태소 토큰
     * @return 의미 있는 품사 여부
     */
    private boolean isMeaningfulToken(Token token) {
        String pos = token.getPos();
        return pos.startsWith("NN") ||  // 명사
                pos.equals("VV") ||      // 동사
                pos.equals("VA") ||      // 형용사
                pos.equals("MAG") ||     // 부사
                pos.equals("IC");        // 감탄사
    }
}