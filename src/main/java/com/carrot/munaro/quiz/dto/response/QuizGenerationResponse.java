package com.carrot.munaro.quiz.dto.response;

import java.util.List;

public record QuizGenerationResponse(
        String title,
        String difficulty,
        List<Question> questions
) {

    public record Question(
            String question,
            String answer,
            List<String> choices
    ) {
    }
}
