package com.moodmate.domain.emotion.repository;

import com.moodmate.domain.emotion.entity.Emotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmotionRepository extends JpaRepository<Emotion, Long> {
    Optional<Emotion> findByName(String name);

    boolean existsByName(String name);
}
