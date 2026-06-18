package com.carrot.munaro.quiz.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record QuizSubmitRequest(

        @NotEmpty(message = "답안은 최소 1개 이상 제출해야 합니다.")
        List<@Valid AnswerRequest> answers
) {

    public record AnswerRequest(

            @NotNull(message = "문제 ID는 필수입니다.")
            Long questionId,

            @NotNull(message = "선택지 ID는 필수입니다.")
            Long choiceId
    ) {
    }
}
