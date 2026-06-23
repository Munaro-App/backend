package com.carrot.munaro.quiz.repository;

import com.carrot.munaro.quiz.domain.QuizSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuizSubmissionRepository
        extends JpaRepository<QuizSubmission, Long> {

    Optional<QuizSubmission> findTopByQuizIdAndUserIdOrderBySubmittedAtDesc(
            Long quizId,
            Long userId
    );

    @Query("""
            SELECT COUNT(submission)
            FROM QuizSubmission submission
            WHERE submission.user.id = :userId
              AND submission.status = com.carrot.munaro.quiz.domain.QuizSubmissionStatus.SUBMITTED
              AND submission.totalQuestionCount = submission.correctAnswerCount
              AND submission.totalQuestionCount > 0
            """)
    long countPerfectSubmissionsByUserId(@Param("userId") Long userId);
}
