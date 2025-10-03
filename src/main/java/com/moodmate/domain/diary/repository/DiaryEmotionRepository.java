package com.moodmate.domain.diary.repository;

import com.moodmate.domain.diary.entity.DiaryEmotion;
import com.moodmate.domain.tracking.dayOfWeek.DayOfWeekEmotionProjection;
import com.moodmate.domain.tracking.frequency.FrequencyDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DiaryEmotionRepository extends JpaRepository<DiaryEmotion, Long> {
    @Query("SELECT new com.moodmate.domain.tracking.frequency.FrequencyDto(de.emotion.name, COUNT(de)) " +
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

    @Query(value = """
        SELECT 
            CAST(EXTRACT(DOW FROM d.date) AS int) as day_of_week,
            e.id as emotion_id,
            e.name as emotion_name,
            COUNT(de.id) as count,
            AVG(de.intensity) as avg_intensity,
            COUNT(DISTINCT d.id) as diary_count
        FROM diary_emotion de
        JOIN diary d ON de.diary_id = d.id
        JOIN emotion e ON de.emotion_id = e.id
        WHERE d.user_id = :userId
        AND d.date BETWEEN :startDate AND :endDate
        GROUP BY EXTRACT(DOW FROM d.date), e.id, e.name
        ORDER BY day_of_week, count DESC
        """, nativeQuery = true)
    List<DayOfWeekEmotionProjection> findDayOfWeekEmotionStats(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
