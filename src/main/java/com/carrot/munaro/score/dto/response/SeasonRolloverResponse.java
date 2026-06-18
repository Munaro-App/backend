package com.carrot.munaro.score.dto.response;

public record SeasonRolloverResponse(
        int expiredSeasonCount,
        int skippedSeasonCount,
        int savedRankingCount
) {
}
