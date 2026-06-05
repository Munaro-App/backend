package com.carrot.munaro.quiz.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "quiz_choices")
public class QuizChoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_choice_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_question_id", nullable = false)
    private QuizQuestion quizQuestion;

    @Column(nullable = false, length = 200)
    private String content;

    @Column(nullable = false)
    private boolean answer;
}