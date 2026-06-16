package com.carrot.munaro.score.repository;

import com.carrot.munaro.score.domain.SeasonWinner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeasonWinnerRepository
        extends JpaRepository<SeasonWinner, Long> {

    boolean existsBySeasonId(Long seasonId);
}
