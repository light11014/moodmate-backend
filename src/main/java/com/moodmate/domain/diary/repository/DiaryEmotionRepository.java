package com.moodmate.domain.diary.repository;

import com.moodmate.domain.diary.entity.DiaryEmotion;
import com.moodmate.domain.tracking.dto.frequency.EmotionFrequencyDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DiaryEmotionRepository extends JpaRepository<DiaryEmotion, Long> {
    @Query("SELECT new com.moodmate.domain.tracking.dto.frequency.EmotionFrequencyDto(de.emotion.name, COUNT(de)) " +
            "FROM DiaryEmotion de " +
            "WHERE de.diary.user.id = :userId " +
            "AND de.diary.date BETWEEN :startDate AND :endDate " +
            "GROUP BY de.emotion " +
            "ORDER BY COUNT(de) DESC")
    List<EmotionFrequencyDto> countEmotionsByPeriod(@Param("userId") Long userId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    @Query("SELECT de.emotion.name, SUM(de.intensity) " +
            "FROM DiaryEmotion de " +
            "WHERE de.diary.user.id = :userId " +
            "AND de.diary.date BETWEEN :startDate AND :endDate " +
            "GROUP BY de.emotion")
    List<Object[]> sumEmotionIntensityByPeriod(@Param("userId") Long userId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);
}
