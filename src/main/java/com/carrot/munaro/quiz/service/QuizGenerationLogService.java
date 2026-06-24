package com.carrot.munaro.quiz.service;

import com.carrot.munaro.quiz.domain.QuizGenerationLog;
import com.carrot.munaro.quiz.domain.QuizGenerationStatus;
import com.carrot.munaro.quiz.repository.QuizGenerationLogRepository;
import com.carrot.munaro.tourist_spot.domain.TouristSpot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class QuizGenerationLogService {

    private final QuizGenerationLogRepository generationLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(
            TouristSpot touristSpot,
            QuizGenerationStatus status,
            String errorMessage
    ) {

        generationLogRepository.save(
                QuizGenerationLog.builder()
                        .touristSpot(touristSpot)
                        .status(status)
                        .errorMessage(errorMessage)
                        .createdAt(OffsetDateTime.now())
                        .build()
        );
    }
}
