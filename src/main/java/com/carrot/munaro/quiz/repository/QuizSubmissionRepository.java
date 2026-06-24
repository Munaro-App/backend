package com.carrot.munaro.quiz.repository;

import com.carrot.munaro.quiz.domain.QuizSubmission;
import com.carrot.munaro.quiz.domain.QuizSubmissionStatus;
import com.carrot.munaro.quiz.repository.projection.QuizHistorySpotRow;
import com.carrot.munaro.quiz.repository.projection.QuizHistorySummaryRow;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizSubmissionRepository
        extends JpaRepository<QuizSubmission, Long> {

    Optional<QuizSubmission> findTopByQuizIdAndUserIdOrderBySubmittedAtDesc(
            Long quizId,
            Long userId
    );

    @EntityGraph(attributePaths = {"quiz", "quiz.touristSpot"})
    List<QuizSubmission>
    findByUserIdAndQuizTouristSpotTouristSpotIdAndStatusOrderBySubmittedAtDesc(
            Long userId,
            Long touristSpotId,
            QuizSubmissionStatus status
    );

    @Query("""
            SELECT
                COALESCE(SUM(submission.score), 0) AS totalEarnedPoints,
                COALESCE(SUM(submission.correctAnswerCount), 0) AS correctCount,
                COALESCE(SUM(submission.totalQuestionCount), 0) AS totalCount,
                COALESCE(SUM(
                    CASE
                        WHEN submission.totalQuestionCount > 0
                         AND submission.totalQuestionCount = submission.correctAnswerCount
                        THEN 1
                        ELSE 0
                    END
                ), 0) AS perfectClearCount
            FROM QuizSubmission submission
            WHERE submission.user.id = :userId
              AND submission.status = com.carrot.munaro.quiz.domain.QuizSubmissionStatus.SUBMITTED
            """)
    QuizHistorySummaryRow findHistorySummaryByUserId(
            @Param("userId") Long userId
    );

    @Query("""
            SELECT
                spot.touristSpotId AS touristSpotId,
                spot.touristSpotName AS touristSpotName,
                COALESCE(SUM(submission.score), 0) AS earnedPoints,
                COALESCE(SUM(submission.correctAnswerCount), 0) AS correctCount,
                COALESCE(SUM(submission.totalQuestionCount), 0) AS totalCount,
                COUNT(submission) AS submissionCount,
                MAX(submission.submittedAt) AS latestSubmittedAt
            FROM QuizSubmission submission
            JOIN submission.quiz quiz
            JOIN quiz.touristSpot spot
            WHERE submission.user.id = :userId
              AND submission.status = com.carrot.munaro.quiz.domain.QuizSubmissionStatus.SUBMITTED
            GROUP BY spot.touristSpotId, spot.touristSpotName
            ORDER BY MAX(submission.submittedAt) DESC
            """)
    List<QuizHistorySpotRow> findHistorySpotRowsByUserId(
            @Param("userId") Long userId
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
