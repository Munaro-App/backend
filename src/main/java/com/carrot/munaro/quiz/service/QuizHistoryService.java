package com.carrot.munaro.quiz.service;

import com.carrot.munaro.global.exception.BusinessException;
import com.carrot.munaro.global.exception.ErrorCode;
import com.carrot.munaro.quiz.domain.QuizSubmission;
import com.carrot.munaro.quiz.domain.QuizSubmissionAnswer;
import com.carrot.munaro.quiz.domain.QuizSubmissionStatus;
import com.carrot.munaro.quiz.dto.response.QuizHistoryResponse;
import com.carrot.munaro.quiz.dto.response.TouristSpotQuizHistoryDetailResponse;
import com.carrot.munaro.quiz.repository.QuizSubmissionAnswerRepository;
import com.carrot.munaro.quiz.repository.QuizSubmissionRepository;
import com.carrot.munaro.quiz.repository.projection.QuizHistorySpotRow;
import com.carrot.munaro.quiz.repository.projection.QuizHistorySummaryRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizHistoryService {

    private final QuizSubmissionRepository quizSubmissionRepository;
    private final QuizSubmissionAnswerRepository
            quizSubmissionAnswerRepository;

    @Transactional(readOnly = true)
    public QuizHistoryResponse getMyQuizHistory(Long userId) {

        QuizHistorySummaryRow summary =
                quizSubmissionRepository.findHistorySummaryByUserId(userId);
        List<QuizHistorySpotRow> rows =
                quizSubmissionRepository.findHistorySpotRowsByUserId(userId);

        return new QuizHistoryResponse(
                toSummaryResponse(summary),
                rows.stream()
                        .map(this::toTouristSpotHistoryResponse)
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public TouristSpotQuizHistoryDetailResponse getMyTouristSpotQuizHistory(
            Long userId,
            Long touristSpotId
    ) {

        List<QuizSubmission> submissions =
                quizSubmissionRepository
                        .findByUserIdAndQuizTouristSpotTouristSpotIdAndStatusOrderBySubmittedAtDesc(
                                userId,
                                touristSpotId,
                                QuizSubmissionStatus.SUBMITTED
                        );

        if (submissions.isEmpty()) {
            throw new BusinessException(ErrorCode.QUIZ_RESULT_NOT_FOUND);
        }

        List<Long> submissionIds = submissions.stream()
                .map(QuizSubmission::getId)
                .toList();
        Map<Long, List<QuizSubmissionAnswer>> answersBySubmissionId =
                getAnswersBySubmissionId(submissionIds);

        int totalEarnedPoints = submissions.stream()
                .mapToInt(QuizSubmission::getScore)
                .sum();
        int correctCount = submissions.stream()
                .mapToInt(QuizSubmission::getCorrectAnswerCount)
                .sum();
        int totalCount = submissions.stream()
                .mapToInt(QuizSubmission::getTotalQuestionCount)
                .sum();
        int perfectClearCount = (int) submissions.stream()
                .filter(this::isPerfect)
                .count();
        QuizSubmission firstSubmission = submissions.get(0);

        return new TouristSpotQuizHistoryDetailResponse(
                touristSpotId,
                firstSubmission.getQuiz()
                        .getTouristSpot()
                        .getTouristSpotName(),
                totalEarnedPoints,
                calculateCorrectRate(correctCount, totalCount),
                perfectClearCount,
                submissions.stream()
                        .map(submission -> toSubmissionHistoryResponse(
                                submission,
                                answersBySubmissionId.getOrDefault(
                                        submission.getId(),
                                        Collections.emptyList()
                                )
                        ))
                        .toList()
        );
    }

    private QuizHistoryResponse.SummaryResponse toSummaryResponse(
            QuizHistorySummaryRow summary
    ) {

        int correctCount = toInt(summary.getCorrectCount());
        int totalCount = toInt(summary.getTotalCount());

        return new QuizHistoryResponse.SummaryResponse(
                toInt(summary.getTotalEarnedPoints()),
                calculateCorrectRate(correctCount, totalCount),
                toInt(summary.getPerfectClearCount())
        );
    }

    private QuizHistoryResponse.TouristSpotQuizHistoryResponse
    toTouristSpotHistoryResponse(QuizHistorySpotRow row) {

        return new QuizHistoryResponse.TouristSpotQuizHistoryResponse(
                row.getTouristSpotId(),
                row.getTouristSpotName(),
                toInt(row.getEarnedPoints()),
                calculateCorrectRate(
                        toInt(row.getCorrectCount()),
                        toInt(row.getTotalCount())
                ),
                toInt(row.getSubmissionCount()),
                row.getLatestSubmittedAt()
        );
    }

    private TouristSpotQuizHistoryDetailResponse.SubmissionHistoryResponse
    toSubmissionHistoryResponse(
            QuizSubmission submission,
            List<QuizSubmissionAnswer> answers
    ) {

        int wrongCount =
                submission.getTotalQuestionCount()
                        - submission.getCorrectAnswerCount();

        return new TouristSpotQuizHistoryDetailResponse
                .SubmissionHistoryResponse(
                submission.getId(),
                submission.getQuiz().getId(),
                submission.getQuiz().getTitle(),
                submission.getScore(),
                submission.getCorrectAnswerCount(),
                wrongCount,
                submission.getTotalQuestionCount(),
                calculateCorrectRate(
                        submission.getCorrectAnswerCount(),
                        submission.getTotalQuestionCount()
                ),
                isPerfect(submission),
                submission.getSubmittedAt(),
                answers.stream()
                        .map(this::toAnswerHistoryResponse)
                        .toList()
        );
    }

    private TouristSpotQuizHistoryDetailResponse.AnswerHistoryResponse
    toAnswerHistoryResponse(QuizSubmissionAnswer answer) {

        return new TouristSpotQuizHistoryDetailResponse.AnswerHistoryResponse(
                answer.getQuizQuestion().getId(),
                answer.getQuizQuestion().getQuestion(),
                answer.getSelectedChoice().getId(),
                answer.getSelectedChoice().getContent(),
                answer.getCorrectChoice().getId(),
                answer.getCorrectChoice().getContent(),
                answer.isCorrect()
        );
    }

    private Map<Long, List<QuizSubmissionAnswer>> getAnswersBySubmissionId(
            List<Long> submissionIds
    ) {

        if (submissionIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return quizSubmissionAnswerRepository
                .findBySubmissionIdInOrderBySubmissionIdDescQuizQuestionIdAsc(
                        submissionIds
                )
                .stream()
                .collect(Collectors.groupingBy(
                        answer -> answer.getSubmission().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private boolean isPerfect(QuizSubmission submission) {

        return submission.getTotalQuestionCount() > 0
                && submission.getTotalQuestionCount()
                == submission.getCorrectAnswerCount();
    }

    private int calculateCorrectRate(int correctCount, int totalCount) {

        if (totalCount <= 0 || correctCount <= 0) {
            return 0;
        }

        return Math.round((float) correctCount * 100 / totalCount);
    }

    private int toInt(Long value) {

        return value == null ? 0 : Math.toIntExact(value);
    }
}
