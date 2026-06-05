package com.carrot.munaro.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private Long userId;
    private String nickname;
    private String email;
    private String avatarValue;
    private String mbti;
    private String bio;
}