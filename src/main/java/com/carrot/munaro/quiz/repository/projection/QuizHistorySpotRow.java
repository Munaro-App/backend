package com.carrot.munaro.quiz.repository.projection;

import java.time.OffsetDateTime;

public interface QuizHistorySpotRow {

    Long getTouristSpotId();

    String getTouristSpotName();

    Long getEarnedPoints();

    Long getCorrectCount();

    Long getTotalCount();

    Long getSubmissionCount();

    OffsetDateTime getLatestSubmittedAt();
}
