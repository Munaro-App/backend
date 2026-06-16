package com.carrot.munaro.score.dto.response;

import java.util.List;

public record RankingPageResponse(
        Long seasonId,
        String seasonName,
        List<RankingItemResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
