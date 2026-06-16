package com.carrot.munaro.score.dto.response;

import com.carrot.munaro.score.domain.Season;

import java.time.OffsetDateTime;

public record SeasonResponse(
        Long seasonId,
        String seasonName,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt,
        OffsetDateTime createdAt
) {

    public static SeasonResponse from(Season season) {
        return new SeasonResponse(
                season.getId(),
                season.getSeasonName(),
                season.getStartedAt(),
                season.getEndedAt(),
                season.getCreatedAt()
        );
    }
}
