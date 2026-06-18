package com.carrot.munaro.quiz.dto.response;

import java.time.OffsetDateTime;

public record QuizSubmitResponse(

        Long quizSubmissionId,
        Long quizId,
        String status,
        int totalQuestionCount,
        int correctAnswerCount,
        int score,
        OffsetDateTime submittedAt
) {
}
