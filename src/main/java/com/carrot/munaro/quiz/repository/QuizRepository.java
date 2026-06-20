package com.carrot.munaro.quiz.repository;

import com.carrot.munaro.quiz.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    boolean existsByTouristSpot_TouristSpotId(Long touristSpotId);
    Optional<Quiz> findByTouristSpot_TouristSpotId(Long touristSpotId);
}
