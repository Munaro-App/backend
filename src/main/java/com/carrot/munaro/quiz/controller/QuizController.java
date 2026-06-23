package com.carrot.munaro.quiz.controller;

import com.carrot.munaro.global.response.ApiResponse;
import com.carrot.munaro.quiz.dto.request.QuizSubmitRequest;
import com.carrot.munaro.quiz.dto.response.QuizResultResponse;
import com.carrot.munaro.quiz.dto.response.QuizResponse;
import com.carrot.munaro.quiz.dto.response.QuizSubmitResponse;
import com.carrot.munaro.quiz.service.QuizGradingService;
import com.carrot.munaro.quiz.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final QuizGradingService quizGradingService;

    @GetMapping("/{quizId}")
    public ApiResponse<QuizResponse> getQuiz(
            @PathVariable Long quizId
    ) {

        return ApiResponse.ok(quizService.getQuiz(quizId));
    }

    @PostMapping("/{quizId}/submit")
    public ApiResponse<QuizSubmitResponse> submitQuiz(
            @PathVariable Long quizId,
            @Valid @RequestBody QuizSubmitRequest request,
            Authentication authentication
    ) {

        Long userId = (Long) authentication.getPrincipal();

        return ApiResponse.ok(
                quizGradingService.submitQuiz(quizId, userId, request)
        );
    }

    @GetMapping("/{quizId}/result")
    public ApiResponse<QuizResultResponse> getLatestResult(
            @PathVariable Long quizId,
            Authentication authentication
    ) {

        Long userId = (Long) authentication.getPrincipal();

        return ApiResponse.ok(
                quizGradingService.getLatestResult(quizId, userId)
        );
    }
}
