package com.carrot.munaro.score.repository;

import com.carrot.munaro.score.domain.Season;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeasonRepository
        extends JpaRepository<Season, Long> {
}