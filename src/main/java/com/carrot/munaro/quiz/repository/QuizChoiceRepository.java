package com.carrot.munaro.quiz.repository;

import com.carrot.munaro.quiz.domain.QuizChoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface QuizChoiceRepository extends JpaRepository<QuizChoice, Long> {

    List<QuizChoice> findByQuizQuestionIdInOrderByIdAsc(
            List<Long> quizQuestionIds
    );

    List<QuizChoice> findByIdIn(Collection<Long> ids);
}
