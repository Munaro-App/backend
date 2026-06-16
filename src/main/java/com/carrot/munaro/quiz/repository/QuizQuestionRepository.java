package com.carrot.munaro.quiz.repository;

import com.carrot.munaro.quiz.domain.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {

    List<QuizQuestion> findByQuizIdOrderByIdAsc(Long quizId);
}
