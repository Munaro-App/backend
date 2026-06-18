package com.carrot.munaro.quiz.service;

import com.carrot.munaro.global.exception.BusinessException;
import com.carrot.munaro.global.exception.ErrorCode;
import com.carrot.munaro.quiz.domain.Quiz;
import com.carrot.munaro.quiz.domain.QuizChoice;
import com.carrot.munaro.quiz.domain.QuizQuestion;
import com.carrot.munaro.quiz.dto.response.QuizResponse;
import com.carrot.munaro.quiz.repository.QuizChoiceRepository;
import com.carrot.munaro.quiz.repository.QuizQuestionRepository;
import com.carrot.munaro.quiz.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizChoiceRepository quizChoiceRepository;
    private final QuizGenerationService quizGenerationService;

    @Transactional(readOnly = true)
    public QuizResponse getQuiz(Long quizId) {

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.QUIZ_NOT_FOUND)
                );

        return toResponse(quiz);
    }

    @Transactional
    public QuizResponse getOrCreateQuizByTouristSpot(Long touristSpotId) {

        Quiz quiz = quizRepository.findByTouristSpotId(touristSpotId)
                .orElseGet(() -> {
                    Long quizId = quizGenerationService
                            .generateAndSaveQuiz(touristSpotId)
                            .quizId();

                    return quizRepository.findById(quizId)
                            .orElseThrow(() ->
                                    new BusinessException(
                                            ErrorCode.QUIZ_NOT_FOUND
                                    )
                            );
                });

        return toResponse(quiz);
    }

    private QuizResponse toResponse(Quiz quiz) {

        List<QuizQuestion> questions =
                quizQuestionRepository.findByQuizIdOrderByIdAsc(quiz.getId());

        List<Long> questionIds = questions.stream()
                .map(QuizQuestion::getId)
                .toList();

        Map<Long, List<QuizChoice>> choicesByQuestionId =
                questionIds.isEmpty()
                        ? Map.of()
                        : quizChoiceRepository
                        .findByQuizQuestionIdInOrderByIdAsc(questionIds)
                        .stream()
                        .collect(Collectors.groupingBy(
                                choice -> choice.getQuizQuestion().getId()
                        ));

        List<QuizResponse.QuestionResponse> questionResponses =
                questions.stream()
                        .map(question -> new QuizResponse.QuestionResponse(
                                question.getId(),
                                question.getQuestion(),
                                toChoiceResponses(
                                        choicesByQuestionId.getOrDefault(
                                                question.getId(),
                                                List.of()
                                        )
                                )
                        ))
                        .toList();

        return new QuizResponse(
                quiz.getId(),
                quiz.getTouristSpot().getTouristSpotId(),
                quiz.getTouristSpot().getTouristSpotName(),
                quiz.getTitle(),
                quiz.getDifficulty().name(),
                questionResponses
        );
    }

    private List<QuizResponse.ChoiceResponse> toChoiceResponses(
            List<QuizChoice> choices
    ) {

        return choices.stream()
                .map(choice -> new QuizResponse.ChoiceResponse(
                        choice.getId(),
                        choice.getContent()
                ))
                .toList();
    }
}
