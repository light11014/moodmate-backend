package com.moodmate.domain.diary.repository;

import com.moodmate.domain.diary.entity.DiaryEmotion;
import com.moodmate.domain.tracking.dto.frequency.FrequencyDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DiaryEmotionRepository extends JpaRepository<DiaryEmotion, Long> {
    @Query("SELECT new com.moodmate.domain.tracking.dto.frequency.FrequencyDto(de.emotion.name, COUNT(de)) " +
            "FROM DiaryEmotion de " +
            "WHERE de.diary.user.id = :userId " +
            "AND de.diary.date BETWEEN :startDate AND :endDate " +
            "GROUP BY de.emotion " +
            "ORDER BY COUNT(de) DESC")
    List<FrequencyDto> countEmotionsByPeriod(@Param("userId") Long userId,
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

    @Query("""
        SELECT e.name, d.date, de.intensity
        FROM DiaryEmotion de
        JOIN de.diary d
        JOIN de.emotion e
        WHERE d.user.id = :userId
          AND d.date BETWEEN :startDate AND :endDate
          AND (:emotions IS NULL OR e.name IN :emotions)
        ORDER BY e.name, d.date, de.id
    """)
    List<Object[]> findEmotionTimeline(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("emotions") List<String> emotions
    );
}
