package com.carrot.munaro.quiz.dto.response;

import java.util.List;

public record QuizResponse(

        Long quizId,
        Long touristSpotId,
        String touristSpotName,
        String title,
        String difficulty,
        List<QuestionResponse> questions
) {

    public record QuestionResponse(

            Long questionId,
            String question,
            List<ChoiceResponse> choices
    ) {
    }

    public record ChoiceResponse(

            Long choiceId,
            String content
    ) {
    }
}
