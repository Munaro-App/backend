package com.carrot.munaro.quiz.dto.response;

public record QuizGenerationResultResponse(
        Long touristSpotId,
        Long quizId,
        int questionCount
) {
}
