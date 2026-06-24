package com.carrot.munaro.quiz.domain;

import com.carrot.munaro.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "quiz_submissions")
public class QuizSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_submission_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuizSubmissionStatus status;

    @Column(name = "total_question_count", nullable = false)
    private int totalQuestionCount;

    @Column(name = "correct_answer_count", nullable = false)
    private int correctAnswerCount;

    @Column(nullable = false)
    private int score;

    @Column(name = "submitted_at", nullable = false)
    private OffsetDateTime submittedAt;

    public void updateProgress(
            QuizSubmissionStatus status,
            int totalQuestionCount,
            int correctAnswerCount,
            int score
    ) {
        this.status = status;
        this.totalQuestionCount = totalQuestionCount;
        this.correctAnswerCount = correctAnswerCount;
        this.score = score;
        this.submittedAt = OffsetDateTime.now();
    }
}
