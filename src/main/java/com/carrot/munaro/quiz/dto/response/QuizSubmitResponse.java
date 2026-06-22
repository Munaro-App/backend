package com.carrot.munaro.quiz.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record QuizSubmitResponse(

        Long submissionId,
        Long quizId,
        Long touristSpotId,
        int correctCount,
        int wrongCount,
        int totalCount,
        int score,
        int earnedPoints,
        boolean perfect,
        boolean passed,
        List<AnswerResultResponse> results
) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AnswerResultResponse(
            Long quizQuestionId,
            boolean correct,
            Long correctChoiceId
    ) {
    }
}
