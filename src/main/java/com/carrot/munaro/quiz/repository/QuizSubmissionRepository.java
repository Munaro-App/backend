package com.carrot.munaro.quiz.repository;

import com.carrot.munaro.quiz.domain.QuizSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizSubmissionRepository
        extends JpaRepository<QuizSubmission, Long> {

    Optional<QuizSubmission> findTopByQuizIdAndUserIdOrderBySubmittedAtDesc(
            Long quizId,
            Long userId
    );
}
