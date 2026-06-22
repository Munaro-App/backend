package com.carrot.munaro.quiz.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "quiz_submission_answers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_qsa_submission_question",
                        columnNames = {"quiz_submission_id", "quiz_question_id"}
                )
        }
)
public class QuizSubmissionAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_submission_answer_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_submission_id", nullable = false)
    private QuizSubmission submission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_question_id", nullable = false)
    private QuizQuestion quizQuestion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_choice_id", nullable = false)
    private QuizChoice selectedChoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "correct_choice_id", nullable = false)
    private QuizChoice correctChoice;

    @Column(nullable = false)
    private boolean correct;

    @Column(name = "answered_at", nullable = false)
    private OffsetDateTime answeredAt;

    public void updateAnswer(
            QuizChoice selectedChoice,
            QuizChoice correctChoice,
            boolean correct
    ) {
        this.selectedChoice = selectedChoice;
        this.correctChoice = correctChoice;
        this.correct = correct;
        this.answeredAt = OffsetDateTime.now();
    }
}
