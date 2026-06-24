package com.carrot.munaro.score.repository;

import com.carrot.munaro.score.domain.Season;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface SeasonRepository
        extends JpaRepository<Season, Long> {

    Optional<Season>
    findFirstByStartedAtLessThanEqualAndEndedAtGreaterThanOrderByEndedAtDesc(
            OffsetDateTime nowForStart,
            OffsetDateTime nowForEnd
    );

    Optional<Season> findFirstByStartedAtAndEndedAt(
            OffsetDateTime startedAt,
            OffsetDateTime endedAt
    );

    Optional<Season> findFirstByStartedAt(OffsetDateTime startedAt);

    List<Season> findByActiveTrueAndEndedAtLessThanEqual(OffsetDateTime now);
}
