package com.carrot.munaro.auth.controller;

import com.carrot.munaro.auth.dto.request.EmailSignUpRequest;
import com.carrot.munaro.auth.dto.request.LoginRequest;
import com.carrot.munaro.auth.service.AuthService;
import com.carrot.munaro.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/email/signup")
    public ApiResponse<Void> signUp(
            @RequestBody EmailSignUpRequest request
    ) {

        authService.signUp(request);

        return ApiResponse.ok(null);
    }

    @PostMapping("/login")
    public ApiResponse<Void> login(
            @RequestBody LoginRequest request
    ) {

        authService.login(request);

        return ApiResponse.ok(null);
    }
}