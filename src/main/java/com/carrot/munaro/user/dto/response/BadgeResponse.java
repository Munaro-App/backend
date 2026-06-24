package com.carrot.munaro.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BadgeResponse {

    private Long badgeId;
    private String badgeName;
    private String badgeType;
    private String badgeImageUrl;
}
