package com.carrot.munaro.quiz.controller;

import com.carrot.munaro.global.response.ApiResponse;
import com.carrot.munaro.quiz.dto.response.QuizGenerationResultResponse;
import com.carrot.munaro.quiz.service.QuizGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/quizzes")
@RequiredArgsConstructor
public class QuizGenerationAdminController {

    private final QuizGenerationService quizGenerationService;

    @PostMapping("/generate/{touristSpotId}")
    public ApiResponse<QuizGenerationResultResponse> generateQuiz(
            @PathVariable Long touristSpotId
    ) {

        return ApiResponse.ok(
                quizGenerationService.generateAndSaveQuiz(touristSpotId)
        );
    }
}
