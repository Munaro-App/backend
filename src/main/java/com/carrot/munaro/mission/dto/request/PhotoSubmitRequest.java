package com.carrot.munaro.mission.dto.request;

public record PhotoSubmitRequest(
        Long photoMissionId,
        String imageUrl
) {
}