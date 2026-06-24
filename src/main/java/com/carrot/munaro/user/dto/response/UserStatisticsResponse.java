package com.carrot.munaro.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserStatisticsResponse {

    private int totalPoints;
    private int completedSpots;
    private int visitedSidoCount;
    private int perfectCount;
}
