package com.carrot.munaro.user.dto.response;

import com.carrot.munaro.user.domain.AvatarType;

public record ProfileUpdateResponse(
        Long userId,
        String nickname,
        AvatarType avatarType,
        String avatarValue
) {
}
