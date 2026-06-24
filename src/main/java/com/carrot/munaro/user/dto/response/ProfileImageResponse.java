package com.carrot.munaro.user.dto.response;

import com.carrot.munaro.user.domain.AvatarType;

public record ProfileImageResponse(
        Long userId,
        AvatarType avatarType,
        String avatarValue
) {
}
