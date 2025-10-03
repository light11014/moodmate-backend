package com.moodmate.domain.tracking.word;

import com.moodmate.domain.diary.entity.Diary;
import com.moodmate.domain.diary.repository.DiaryRepository;
import com.moodmate.domain.tracking.word.dto.WordFrequency;
import com.moodmate.domain.tracking.word.dto.WordFrequencyMeta;
import com.moodmate.domain.tracking.word.dto.WordFrequencyResponse;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class WordAnalysisService {

    private final Komoran komoran = new Komoran(DEFAULT_MODEL.LIGHT);
    private final DiaryRepository diaryRepository;

    private static final Set<String> KOREAN_STOP_WORDS = Set.of(
            "것", "수", "등", "및", "제", "약", "전", "후", "간", "중"
    );

    public WordFrequencyResponse getFrequentWords(Long userId, LocalDate startDate, LocalDate endDate, int limit) {
        List<Diary> diaries = diaryRepository.findByUserIdAndDateBetween(userId, startDate, endDate);

        Map<String, Long> wordFrequency = new HashMap<>();
        long totalWordCount = 0;

        for (Diary diary : diaries) {
            String content = diary.getContent();

            // 1. 한국어 추출 (Komoran)
            KomoranResult result = komoran.analyze(content);

            List<String> meaningfulWords = result.getTokenList().stream()
                    .filter(token -> {
                        String pos = token.getPos();
                        return pos.startsWith("NN") ||  // 명사
                                pos.equals("VV") ||      // 동사
                                pos.equals("VA") ||      // 형용사
                                pos.equals("MAG") ||     // 부사
                                pos.equals("IC");        // 감탄사
                    })
                    .map(token -> token.getMorph())
                    .filter(word -> word.length() > 1)
                    .filter(word -> !KOREAN_STOP_WORDS.contains(word))
                    .toList();

            meaningfulWords.forEach(word -> wordFrequency.merge(word, 1L, Long::sum));
            totalWordCount += meaningfulWords.size();

            // 2. 영어 단어 추출 (정규식)
            Pattern englishPattern = Pattern.compile("[a-zA-Z]{2,}");
            Matcher matcher = englishPattern.matcher(content);
            while (matcher.find()) {
                String word = matcher.group().toLowerCase();
                if (!isEnglishStopWord(word)) {
                    wordFrequency.merge(word, 1L, Long::sum);
                    totalWordCount++;
                }
            }
        }

        List<WordFrequency> topWords = wordFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> new WordFrequency(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        WordFrequencyMeta meta = new WordFrequencyMeta(
                userId,
                startDate,
                endDate,
                (long) diaries.size(),
                totalWordCount,
                wordFrequency.size(),
                LocalDateTime.now()
        );

        return new WordFrequencyResponse(meta, topWords);
    }

    private boolean isEnglishStopWord(String word) {
        Set<String> stopWords = Set.of("the", "is", "am", "are", "was", "were");
        return stopWords.contains(word);
    }
}
