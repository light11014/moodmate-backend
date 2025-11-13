package com.moodmate.domain.diary.repository;

import com.moodmate.domain.diary.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    // 날짜와 사용자 ID로 일기 한 개 가져오기
    Optional<Diary> findByUserIdAndDate(Long userId, LocalDate date);
    List<Diary> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    Integer countByUserIdAndDateBetween(long userId, LocalDate startDate, LocalDate endDate);

    Integer countByUserId(long userId);
}
