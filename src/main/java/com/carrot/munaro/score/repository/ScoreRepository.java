package com.carrot.munaro.score.repository;

import com.carrot.munaro.score.domain.Score;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScoreRepository
        extends JpaRepository<Score, Long> {
}