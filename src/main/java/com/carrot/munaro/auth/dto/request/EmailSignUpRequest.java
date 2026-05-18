package com.carrot.munaro.auth.dto.request;

public record EmailSignUpRequest(

        String email,
        String password,
        String nickname

) {
}