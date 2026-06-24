package com.carrot.munaro.quiz.repository;

import com.carrot.munaro.quiz.domain.QuizGenerationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizGenerationLogRepository
        extends JpaRepository<QuizGenerationLog, Long> {
}
