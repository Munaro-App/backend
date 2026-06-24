package com.carrot.munaro.user.dto.request;

import com.carrot.munaro.user.domain.AvatarType;

public record ProfileUpdateRequest(
        String nickname,
        AvatarType avatarType,
        String avatarValue
) {
}
