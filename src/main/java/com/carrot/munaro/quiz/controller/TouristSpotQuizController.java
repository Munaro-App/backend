package com.carrot.munaro.quiz.controller;

import com.carrot.munaro.global.response.ApiResponse;
import com.carrot.munaro.quiz.dto.response.QuizResponse;
import com.carrot.munaro.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tourist-spots")
@RequiredArgsConstructor
public class TouristSpotQuizController {

    private final QuizService quizService;

    @PostMapping("/{spotId}/quizzes")
    public ApiResponse<QuizResponse> getOrCreateQuiz(
            @PathVariable Long spotId
    ) {

        return ApiResponse.ok(
                quizService.getOrCreateQuizByTouristSpot(spotId)
        );
    }
}
