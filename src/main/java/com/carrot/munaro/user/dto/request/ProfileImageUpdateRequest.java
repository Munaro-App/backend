package com.carrot.munaro.user.dto.request;

import com.carrot.munaro.user.domain.AvatarType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProfileImageUpdateRequest(

        @NotNull(message = "아바타 타입은 필수입니다.")
        AvatarType avatarType,

        @NotBlank(message = "프로필 이미지 값은 필수입니다.")
        String avatarValue
) {
}
