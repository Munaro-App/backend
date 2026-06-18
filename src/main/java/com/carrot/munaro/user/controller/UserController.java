package com.carrot.munaro.user.controller;

import com.carrot.munaro.user.dto.response.UserResponse;
import com.carrot.munaro.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
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
}
