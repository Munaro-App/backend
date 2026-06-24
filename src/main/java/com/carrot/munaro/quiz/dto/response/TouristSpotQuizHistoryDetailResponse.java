package com.carrot.munaro.quiz.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public record TouristSpotQuizHistoryDetailResponse(
        Long touristSpotId,
        String touristSpotName,
        int totalEarnedPoints,
        int averageCorrectRate,
        int perfectClearCount,
        List<SubmissionHistoryResponse> submissions
) {

    public record SubmissionHistoryResponse(
            Long submissionId,
            Long quizId,
            String quizTitle,
            int earnedPoints,
            int correctCount,
            int wrongCount,
            int totalCount,
            int correctRate,
            boolean perfect,
            OffsetDateTime submittedAt,
            List<AnswerHistoryResponse> answers
    ) {
    }

    public record AnswerHistoryResponse(
            Long quizQuestionId,
            String question,
            Long selectedChoiceId,
            String selectedChoiceContent,
            Long correctChoiceId,
            String correctChoiceContent,
            boolean correct
    ) {
    }
}
