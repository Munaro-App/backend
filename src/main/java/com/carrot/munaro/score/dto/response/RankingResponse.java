package com.carrot.munaro.score.dto.response;

import java.util.List;

public record RankingResponse(
        Long seasonId,
        String seasonName,
        List<RankingItemResponse> rankings
) {
}
