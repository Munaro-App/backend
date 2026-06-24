package com.carrot.munaro.quiz.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public record QuizHistoryResponse(
        SummaryResponse summary,
        List<TouristSpotQuizHistoryResponse> touristSpots
) {

    public record SummaryResponse(
            int totalEarnedPoints,
            int averageCorrectRate,
            int perfectClearCount
    ) {
    }

    public record TouristSpotQuizHistoryResponse(
            Long touristSpotId,
            String touristSpotName,
            int earnedPoints,
            int correctRate,
            int submissionCount,
            OffsetDateTime latestSubmittedAt
    ) {
    }
}
