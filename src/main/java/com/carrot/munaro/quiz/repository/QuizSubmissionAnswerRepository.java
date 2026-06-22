package com.carrot.munaro.quiz.repository;

import com.carrot.munaro.quiz.domain.QuizSubmissionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizSubmissionAnswerRepository
        extends JpaRepository<QuizSubmissionAnswer, Long> {

    Optional<QuizSubmissionAnswer> findBySubmissionIdAndQuizQuestionId(
            Long submissionId,
            Long quizQuestionId
    );

    List<QuizSubmissionAnswer> findBySubmissionIdOrderByQuizQuestionIdAsc(
            Long submissionId
    );

    long countBySubmissionId(Long submissionId);

    long countBySubmissionIdAndCorrectTrue(Long submissionId);
}
