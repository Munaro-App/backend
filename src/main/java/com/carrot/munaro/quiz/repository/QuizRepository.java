package com.carrot.munaro.quiz.repository;

import com.carrot.munaro.quiz.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    boolean existsByTouristSpotId(Long touristSpotId);
}
