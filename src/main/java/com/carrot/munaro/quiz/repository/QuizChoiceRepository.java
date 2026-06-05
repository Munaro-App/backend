package com.carrot.munaro.quiz.repository;

import com.carrot.munaro.quiz.domain.QuizChoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizChoiceRepository extends JpaRepository<QuizChoice, Long> {
}