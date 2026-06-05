package com.carrot.munaro.quiz.repository;

import com.carrot.munaro.quiz.domain.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
}