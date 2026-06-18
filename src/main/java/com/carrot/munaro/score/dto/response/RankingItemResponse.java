package com.carrot.munaro.score.dto.response;

public record RankingItemResponse(
        Long userId,
        String nickname,
        Integer score,
        Integer rank
) {
}