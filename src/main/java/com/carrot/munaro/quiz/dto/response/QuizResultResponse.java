package com.carrot.munaro.quiz.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public record QuizResultResponse(

        Long quizSubmissionId,
        Long quizId,
        Long touristSpotId,
        String status,
        int totalCount,
        int submittedCount,
        int correctCount,
        int wrongCount,
        int score,
        int earnedPoints,
        boolean perfect,
        boolean passed,
        List<QuizSubmitResponse.AnswerResultResponse> results,
        OffsetDateTime submittedAt
) {
}
