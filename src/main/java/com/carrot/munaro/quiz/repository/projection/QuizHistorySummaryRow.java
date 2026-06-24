package com.carrot.munaro.quiz.repository.projection;

public interface QuizHistorySummaryRow {

    Long getTotalEarnedPoints();

    Long getCorrectCount();

    Long getTotalCount();

    Long getPerfectClearCount();
}
