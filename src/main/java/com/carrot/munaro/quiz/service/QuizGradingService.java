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
import com.carrot.munaro.quiz.repository.QuizSubmissionAnswerRepository;
import com.carrot.munaro.quiz.repository.QuizSubmissionRepository;
import com.carrot.munaro.score.domain.ScoreSource;
import com.carrot.munaro.score.service.ScoreService;
import com.carrot.munaro.user.domain.User;
import com.carrot.munaro.user.domain.UserVisitedTouristSpot;
import com.carrot.munaro.user.repository.UserRepository;
import com.carrot.munaro.user.repository.UserVisitedTouristSpotRepository;
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

    private static final int POINTS_PER_QUESTION = 15;
    private static final int PASSING_SCORE = 60;

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizChoiceRepository quizChoiceRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final QuizSubmissionAnswerRepository
            quizSubmissionAnswerRepository;
    private final UserRepository userRepository;
    private final UserVisitedTouristSpotRepository
            userVisitedTouristSpotRepository;
    private final ScoreService scoreService;

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
                .map(QuizSubmitRequest.AnswerRequest::quizChoiceId)
                .toList();
        Map<Long, QuizChoice> choiceById =
                quizChoiceRepository.findByIdIn(choiceIds)
                        .stream()
                        .collect(Collectors.toMap(
                                QuizChoice::getId,
                                Function.identity()
                        ));

        List<AnswerGrading> gradings =
                request.answers()
                        .stream()
                        .map(answer -> gradeAnswer(
                                quiz,
                                questionById,
                                choiceById,
                                answer
                        ))
                        .toList();
        int correctCount = (int) gradings.stream()
                .filter(AnswerGrading::correct)
                .count();

        QuizSubmission submission = saveSubmission(
                quiz,
                user,
                QuizSubmissionStatus.SUBMITTED,
                questions.size(),
                correctCount
        );
        saveSubmissionAnswers(submission, gradings);
        saveVisitedTouristSpotIfAbsent(quiz, user);
        scoreService.addScore(
                userId,
                submission.getScore(),
                ScoreSource.QUIZ
        );

        return toSubmitResponse(
                submission,
                gradings.stream()
                        .map(this::toAnswerResultResponse)
                        .toList()
        );
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

        return toSubmitResponse(submission, null);
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

        List<QuizSubmissionAnswer> answers =
                quizSubmissionAnswerRepository
                        .findBySubmissionIdOrderByQuizQuestionIdAsc(
                                submission.getId()
                        );

        return toResultResponse(submission, answers);
    }

    private void validateAnswerCount(
            QuizSubmitRequest request,
            List<QuizQuestion> questions
    ) {

        Set<Long> submittedQuestionIds = request.answers()
                .stream()
                .map(QuizSubmitRequest.AnswerRequest::quizQuestionId)
                .collect(Collectors.toSet());

        if (submittedQuestionIds.size() != request.answers().size()
                || submittedQuestionIds.size() != questions.size()) {
            throw new BusinessException(ErrorCode.QUIZ_ANSWER_INVALID);
        }
    }

    private AnswerGrading gradeAnswer(
            Quiz quiz,
            Map<Long, QuizQuestion> questionById,
            Map<Long, QuizChoice> choiceById,
            QuizSubmitRequest.AnswerRequest answer
    ) {

        QuizQuestion question = questionById.get(answer.quizQuestionId());
        QuizChoice selectedChoice = choiceById.get(answer.quizChoiceId());

        if (question == null || selectedChoice == null) {
            throw new BusinessException(ErrorCode.QUIZ_ANSWER_INVALID);
        }

        validateQuestionAndChoice(quiz, question, selectedChoice);

        QuizChoice correctChoice = getCorrectChoice(question.getId());

        return new AnswerGrading(
                question,
                selectedChoice,
                correctChoice,
                selectedChoice.isAnswer()
        );
    }

    private QuizSubmission saveSubmission(
            Quiz quiz,
            User user,
            QuizSubmissionStatus status,
            int totalQuestionCount,
            int correctAnswerCount
    ) {

        int score = calculateEarnedPoints(correctAnswerCount);

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
            int totalQuestionCount,
            int correctAnswerCount
    ) {

        if (totalQuestionCount <= 0 || correctAnswerCount <= 0) {
            return 0;
        }

        return Math.round(
                (float) correctAnswerCount * 100 / totalQuestionCount
        );
    }

    private int calculateEarnedPoints(int correctAnswerCount) {

        return correctAnswerCount * POINTS_PER_QUESTION;
    }

    private Quiz getQuiz(Long quizId) {

        return quizRepository.findById(quizId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.QUIZ_NOT_FOUND)
                );
    }

    private QuizQuestion getQuestion(Long quizQuestionId) {

        return quizQuestionRepository.findById(quizQuestionId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.QUIZ_ANSWER_INVALID)
                );
    }

    private QuizChoice getChoice(Long quizChoiceId) {

        return quizChoiceRepository.findById(quizChoiceId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.QUIZ_ANSWER_INVALID)
                );
    }

    private User getUser(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND)
                );
    }

    private void saveSubmissionAnswers(
            QuizSubmission submission,
            List<AnswerGrading> gradings
    ) {

        List<QuizSubmissionAnswer> answers = gradings.stream()
                .map(grading -> QuizSubmissionAnswer.builder()
                        .submission(submission)
                        .quizQuestion(grading.question())
                        .selectedChoice(grading.selectedChoice())
                        .correctChoice(grading.correctChoice())
                        .correct(grading.correct())
                        .answeredAt(OffsetDateTime.now())
                        .build())
                .toList();

        quizSubmissionAnswerRepository.saveAll(answers);
    }

    private QuizSubmitResponse toSubmitResponse(
            QuizSubmission submission,
            List<QuizSubmitResponse.AnswerResultResponse> results
    ) {

        int score = calculateScore(
                submission.getTotalQuestionCount(),
                submission.getCorrectAnswerCount()
        );

        return new QuizSubmitResponse(
                submission.getId(),
                submission.getQuiz().getId(),
                submission.getQuiz().getTouristSpot().getTouristSpotId(),
                submission.getCorrectAnswerCount(),
                submission.getTotalQuestionCount()
                        - submission.getCorrectAnswerCount(),
                submission.getTotalQuestionCount(),
                score,
                submission.getScore(),
                submission.getTotalQuestionCount() > 0
                        && submission.getTotalQuestionCount()
                        == submission.getCorrectAnswerCount(),
                score >= PASSING_SCORE,
                results
        );
    }

    private QuizResultResponse toResultResponse(
            QuizSubmission submission,
            List<QuizSubmissionAnswer> answers
    ) {

        int submittedCount = answers.size();
        int wrongCount =
                submission.getCorrectAnswerCount() > submittedCount
                        ? 0
                        : submittedCount - submission.getCorrectAnswerCount();
        int score = calculateScore(
                submission.getTotalQuestionCount(),
                submission.getCorrectAnswerCount()
        );

        return new QuizResultResponse(
                submission.getId(),
                submission.getQuiz().getId(),
                submission.getQuiz().getTouristSpot().getTouristSpotId(),
                submission.getStatus().name(),
                submission.getTotalQuestionCount(),
                submittedCount,
                submission.getCorrectAnswerCount(),
                wrongCount,
                score,
                submission.getScore(),
                submission.getTotalQuestionCount() > 0
                        && submission.getTotalQuestionCount()
                        == submission.getCorrectAnswerCount(),
                score >= PASSING_SCORE,
                answers.stream()
                        .map(this::toAnswerResultResponse)
                        .toList(),
                submission.getSubmittedAt()
        );
    }

    private QuizSubmitResponse.AnswerResultResponse toAnswerResultResponse(
            AnswerGrading grading
    ) {

        return new QuizSubmitResponse.AnswerResultResponse(
                grading.question().getId(),
                grading.correct(),
                grading.correct() ? null : grading.correctChoice().getId()
        );
    }

    private QuizSubmitResponse.AnswerResultResponse toAnswerResultResponse(
            QuizSubmissionAnswer answer
    ) {

        return new QuizSubmitResponse.AnswerResultResponse(
                answer.getQuizQuestion().getId(),
                answer.isCorrect(),
                answer.isCorrect() ? null : answer.getCorrectChoice().getId()
        );
    }

    private record AnswerGrading(
            QuizQuestion question,
            QuizChoice selectedChoice,
            QuizChoice correctChoice,
            boolean correct
    ) {
    }

    private QuizChoice getCorrectChoice(Long questionId) {

        return quizChoiceRepository.findByQuizQuestionIdInOrderByIdAsc(
                        List.of(questionId)
                )
                .stream()
                .filter(QuizChoice::isAnswer)
                .findFirst()
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.QUIZ_ANSWER_INVALID)
                );
    }

    private void validateQuestionAndChoice(
            Quiz quiz,
            QuizQuestion question,
            QuizChoice choice
    ) {

        if (!question.getQuiz().getId().equals(quiz.getId()) ||
                !choice.getQuizQuestion().getId().equals(question.getId())) {
            throw new BusinessException(ErrorCode.QUIZ_ANSWER_INVALID);
        }
    }

    private void saveVisitedTouristSpotIfAbsent(
            Quiz quiz,
            User user
    ) {

        Long touristSpotId = quiz.getTouristSpot().getTouristSpotId();

        if (userVisitedTouristSpotRepository
                .existsByUser_IdAndTouristSpot_TouristSpotId(
                        user.getId(),
                        touristSpotId
                )) {
            return;
        }

        userVisitedTouristSpotRepository.save(
                UserVisitedTouristSpot.builder()
                        .user(user)
                        .touristSpot(quiz.getTouristSpot())
                        .visitedAt(OffsetDateTime.now())
                        .build()
        );
    }
}
