package com.carrot.munaro.user.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserResponse {

    private Long userId;
    private String nickname;
    private String avatarValue;
    private Integer seasonRank;
    private UserStatisticsResponse statistics;
    private List<String> visitedSidos;
    private List<BadgeResponse> recentBadges;
}
