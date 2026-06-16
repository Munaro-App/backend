package com.carrot.munaro.quiz.service;

import com.carrot.munaro.global.exception.BusinessException;
import com.carrot.munaro.global.exception.ErrorCode;
import com.carrot.munaro.quiz.domain.*;
import com.carrot.munaro.quiz.dto.response.QuizGenerationResponse;
import com.carrot.munaro.quiz.dto.response.QuizGenerationResultResponse;
import com.carrot.munaro.quiz.repository.QuizChoiceRepository;
import com.carrot.munaro.quiz.repository.QuizGenerationLogRepository;
import com.carrot.munaro.quiz.repository.QuizQuestionRepository;
import com.carrot.munaro.quiz.repository.QuizRepository;
import com.carrot.munaro.tourist_spot.domain.TouristSpot;
import com.carrot.munaro.tourist_spot.repository.TouristSpotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizGenerationService {

    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MILLIS = 3000L;

    private final TouristSpotRepository touristSpotRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizChoiceRepository quizChoiceRepository;
    private final QuizGenerationLogRepository generationLogRepository;
    private final OpenAIQuizService openAIQuizService;
    private final QuizGenerationLogService quizGenerationLogService;

    @Transactional
    public QuizGenerationResultResponse generateAndSaveQuiz(
            Long touristSpotId
    ) {

        TouristSpot touristSpot =
                touristSpotRepository.findById(touristSpotId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.TOURIST_SPOT_NOT_FOUND
                                )
                        );

        if (quizRepository.existsByTouristSpotId(touristSpotId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        try {
            QuizGenerationResponse response =
                    generateWithRetry(touristSpot);
            Quiz quiz = saveQuiz(touristSpot, response);

            quizGenerationLogService.saveLog(
                    touristSpot,
                    QuizGenerationStatus.SUCCESS,
                    null
            );

            return new QuizGenerationResultResponse(
                    touristSpot.getId(),
                    quiz.getId(),
                    response.questions().size()
            );
        } catch (Exception e) {
            log.error("OpenAI quiz generation failed.", e);
            quizGenerationLogService.saveLog(
                    touristSpot,
                    QuizGenerationStatus.FAILED,
                    rootCauseMessage(e)
            );
            throw new BusinessException(ErrorCode.QUIZ_GENERATION_FAILED);
        }
    }

    private QuizGenerationResponse generateWithRetry(
            TouristSpot touristSpot
    ) {

        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                QuizGenerationResponse response =
                        openAIQuizService.generateQuiz(touristSpot);
                validateResponse(response);
                return response;
            } catch (Exception e) {
                lastException = e;
                log.warn(
                        "Quiz generation attempt {} failed. touristSpotId={}",
                        attempt,
                        touristSpot.getId(),
                        e
                );
                sleepBeforeRetry(attempt);
            }
        }

        throw new IllegalStateException(
                "Quiz generation failed after retries.",
                lastException
        );
    }

    private void validateResponse(QuizGenerationResponse response) {

        if (response == null
                || response.title() == null
                || response.title().isBlank()
                || response.questions() == null
                || response.questions().size() != 3) {
            throw new IllegalStateException("Invalid quiz response.");
        }

        for (QuizGenerationResponse.Question question
                : response.questions()) {
            if (question.question() == null
                    || question.question().isBlank()
                    || question.answer() == null
                    || question.answer().isBlank()
                    || question.choices() == null
                    || question.choices().size() != 4
                    || !question.choices().contains(question.answer())) {
                throw new IllegalStateException(
                        "Invalid quiz question response."
                );
            }
        }
    }

    private Quiz saveQuiz(
            TouristSpot touristSpot,
            QuizGenerationResponse response
    ) {

        Quiz quiz = quizRepository.save(
                Quiz.builder()
                        .touristSpot(touristSpot)
                        .title(response.title())
                        .difficulty(parseDifficulty(response.difficulty()))
                        .build()
        );

        for (QuizGenerationResponse.Question generatedQuestion
                : response.questions()) {
            QuizQuestion question = quizQuestionRepository.save(
                    QuizQuestion.builder()
                            .quiz(quiz)
                            .question(generatedQuestion.question())
                            .build()
            );

            saveChoices(question, generatedQuestion);
        }

        return quiz;
    }

    private void saveChoices(
            QuizQuestion question,
            QuizGenerationResponse.Question generatedQuestion
    ) {

        List<QuizChoice> choices =
                generatedQuestion.choices()
                        .stream()
                        .map(choice -> QuizChoice.builder()
                                .quizQuestion(question)
                                .content(choice)
                                .answer(choice.equals(
                                        generatedQuestion.answer()
                                ))
                                .build())
                        .toList();

        quizChoiceRepository.saveAll(choices);
    }

    private QuizDifficulty parseDifficulty(String difficulty) {

        try {
            return QuizDifficulty.valueOf(difficulty);
        } catch (Exception e) {
            return QuizDifficulty.NORMAL;
        }
    }

    private void sleepBeforeRetry(int attempt) {

        if (attempt >= MAX_ATTEMPTS) {
            return;
        }

        try {
            Thread.sleep(RETRY_DELAY_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Retry sleep interrupted.", e);
        }
    }

    private String rootCauseMessage(Exception exception) {

        Throwable current = exception;

        while (current.getCause() != null) {
            current = current.getCause();
        }

        String message = current.getMessage();

        if (message == null || message.isBlank()) {
            return current.getClass().getSimpleName();
        }

        return message;
    }
}
