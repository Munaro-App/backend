package com.carrot.munaro.auth.controller;

import com.carrot.munaro.auth.dto.request.EmailSignUpRequest;
import com.carrot.munaro.auth.dto.request.GoogleLoginRequest;
import com.carrot.munaro.auth.dto.request.LoginRequest;
import com.carrot.munaro.auth.dto.response.LoginResponse;
import com.carrot.munaro.auth.service.AuthService;
import com.carrot.munaro.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/email/signup")
    public ApiResponse<Void> signUp(
            @Valid @RequestBody EmailSignUpRequest request
    ) {

        authService.signUp(request);

        return ApiResponse.ok(null);
    }

    @PostMapping("/email/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/google/login")
    public ApiResponse<LoginResponse> googleLogin(

            @Valid @RequestBody
            GoogleLoginRequest request

    ) {

        LoginResponse response =
                authService.googleLogin(request);

        return ApiResponse.ok(response);
    }
}