package com.carrot.munaro.user.controller;

import com.carrot.munaro.global.response.ApiResponse;
import com.carrot.munaro.user.dto.request.ProfileImageUpdateRequest;
import com.carrot.munaro.user.dto.response.ProfileImageResponse;
import com.carrot.munaro.user.dto.response.UserResponse;
import com.carrot.munaro.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {

        Long userId =
                (Long) authentication.getPrincipal();

        return userService.getMe(userId);
    }

    @PatchMapping("/me/profile/avatar/upload-url")
    public ApiResponse<ProfileImageResponse> updateProfileImage(
            Authentication authentication,
            @Valid @RequestBody ProfileImageUpdateRequest request
    ) {

        Long userId = (Long) authentication.getPrincipal();

        return ApiResponse.ok(
                userService.updateProfileImage(userId, request)
        );
    }

    @DeleteMapping("/me/profile/avatar")
    public ApiResponse<ProfileImageResponse> deleteProfileImage(
            Authentication authentication
    ) {

        Long userId = (Long) authentication.getPrincipal();

        return ApiResponse.ok(
                userService.deleteProfileImage(userId)
        );
    }
}
