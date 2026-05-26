package com.carrot.munaro.auth.dto.response;

import lombok.Getter;

@Getter
public class GoogleUserResponse {

    private String sub;
    private String email;
    private String name;
}