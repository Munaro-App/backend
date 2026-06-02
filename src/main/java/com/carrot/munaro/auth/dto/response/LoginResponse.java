package com.carrot.munaro.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private UserInfo user;

    @Getter
    @Builder
    public static class UserInfo {

        private Long id;
        private String nickname;
        private String userRole;
        private String userStatus;
        private boolean isNewUser;
        private boolean dogSetupRequired;
    }
}