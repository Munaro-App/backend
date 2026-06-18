package com.carrot.munaro.quiz.service;

import com.carrot.munaro.global.exception.BusinessException;
import com.carrot.munaro.global.exception.ErrorCode;
import com.carrot.munaro.quiz.domain.*;
import com.carrot.munaro.quiz.dto.request.QuizSubmitRequest;
import com.carrot.munaro.quiz.dto.response.QuizResultResponse;
import com.carrot.munaro.quiz.dto.response.QuizSubmitResponse;
import com.carrot.munaro.quiz.repository.QuizChoiceRepository;
import com.carrot.munaro.quiz.repository.QuizQuestionRepository;
import com.carrot.munaro.quiz.repository.QuizRepository;
import com.carrot.munaro.quiz.repository.QuizSubmissionRepository;
import com.carrot.munaro.user.domain.User;
import com.carrot.munaro.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizGradingService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizChoiceRepository quizChoiceRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final UserRepository userRepository;

    @Transactional
    public QuizSubmitResponse submitQuiz(
            Long quizId,
            Long userId,
            QuizSubmitRequest request
    ) {

        Quiz quiz = getQuiz(quizId);
        User user = getUser(userId);
        List<QuizQuestion> questions =
                quizQuestionRepository.findByQuizIdOrderByIdAsc(quizId);

        validateAnswerCount(request, questions);

        Map<Long, QuizQuestion> questionById = questions.stream()
                .collect(Collectors.toMap(
                        QuizQuestion::getId,
                        Function.identity()
                ));

        List<Long> choiceIds = request.answers()
                .stream()
                .map(QuizSubmitRequest.AnswerRequest::choiceId)
                .toList();

        Map<Long, QuizChoice> choiceById =
                quizChoiceRepository.findByIdIn(choiceIds)
                        .stream()
                        .collect(Collectors.toMap(
                                QuizChoice::getId,
                                Function.identity()
                        ));

        int correctAnswerCount = 0;

        for (QuizSubmitRequest.AnswerRequest answer : request.answers()) {
            QuizQuestion question = questionById.get(answer.questionId());
            QuizChoice choice = choiceById.get(answer.choiceId());

            if (question == null ||
                    choice == null ||
                    !choice.getQuizQuestion().getId().equals(question.getId())) {
                throw new BusinessException(ErrorCode.QUIZ_ANSWER_INVALID);
            }

            if (choice.isAnswer()) {
                correctAnswerCount++;
            }
        }

        QuizSubmission submission = saveSubmission(
                quiz,
                user,
                QuizSubmissionStatus.SUBMITTED,
                questions.size(),
                correctAnswerCount
        );

        return toSubmitResponse(submission);
    }

    @Transactional
    public QuizSubmitResponse skipQuiz(
            Long quizId,
            Long userId
    ) {

        Quiz quiz = getQuiz(quizId);
        User user = getUser(userId);
        int totalQuestionCount =
                quizQuestionRepository.findByQuizIdOrderByIdAsc(quizId)
                        .size();

        QuizSubmission submission = saveSubmission(
                quiz,
                user,
                QuizSubmissionStatus.SKIPPED,
                totalQuestionCount,
                0
        );

        return toSubmitResponse(submission);
    }

    @Transactional(readOnly = true)
    public QuizResultResponse getLatestResult(
            Long quizId,
            Long userId
    ) {

        QuizSubmission submission =
                quizSubmissionRepository
                        .findTopByQuizIdAndUserIdOrderBySubmittedAtDesc(
                                quizId,
                                userId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.QUIZ_RESULT_NOT_FOUND
                                )
                        );

        return toResultResponse(submission);
    }

    private void validateAnswerCount(
            QuizSubmitRequest request,
            List<QuizQuestion> questions
    ) {

        Set<Long> submittedQuestionIds = request.answers()
                .stream()
                .map(QuizSubmitRequest.AnswerRequest::questionId)
                .collect(Collectors.toSet());

        if (submittedQuestionIds.size() != request.answers().size() ||
                submittedQuestionIds.size() != questions.size()) {
            throw new BusinessException(ErrorCode.QUIZ_ANSWER_INVALID);
        }
    }

    private QuizSubmission saveSubmission(
            Quiz quiz,
            User user,
            QuizSubmissionStatus status,
            int totalQuestionCount,
            int correctAnswerCount
    ) {

        int score = calculateScore(
                quiz.getDifficulty(),
                totalQuestionCount,
                correctAnswerCount
        );

        return quizSubmissionRepository.save(
                QuizSubmission.builder()
                        .quiz(quiz)
                        .user(user)
                        .status(status)
                        .totalQuestionCount(totalQuestionCount)
                        .correctAnswerCount(correctAnswerCount)
                        .score(score)
                        .submittedAt(OffsetDateTime.now())
                        .build()
        );
    }

    private int calculateScore(
            QuizDifficulty difficulty,
            int totalQuestionCount,
            int correctAnswerCount
    ) {

        if (totalQuestionCount <= 0 || correctAnswerCount <= 0) {
            return 0;
        }

        int maxScore = switch (difficulty) {
            case HARD -> 50;
            case NORMAL -> 30;
            case EASY -> 20;
        };

        return Math.round(
                (float) maxScore * correctAnswerCount / totalQuestionCount
        );
    }

    private Quiz getQuiz(Long quizId) {

        return quizRepository.findById(quizId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.QUIZ_NOT_FOUND)
                );
    }

    private User getUser(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND)
                );
    }

    private QuizSubmitResponse toSubmitResponse(
            QuizSubmission submission
    ) {

        return new QuizSubmitResponse(
                submission.getId(),
                submission.getQuiz().getId(),
                submission.getStatus().name(),
                submission.getTotalQuestionCount(),
                submission.getCorrectAnswerCount(),
                submission.getScore(),
                submission.getSubmittedAt()
        );
    }

    private QuizResultResponse toResultResponse(
            QuizSubmission submission
    ) {

        return new QuizResultResponse(
                submission.getId(),
                submission.getQuiz().getId(),
                submission.getStatus().name(),
                submission.getTotalQuestionCount(),
                submission.getCorrectAnswerCount(),
                submission.getScore(),
                submission.getSubmittedAt()
        );
    }
}
