package com.carrot.munaro.auth.service;

import com.carrot.munaro.auth.dto.request.EmailSignUpRequest;
import com.carrot.munaro.auth.dto.request.LoginRequest;
import com.carrot.munaro.user.domain.AuthProvider;
import com.carrot.munaro.user.domain.User;
import com.carrot.munaro.user.domain.UserRole;
import com.carrot.munaro.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public void signUp(EmailSignUpRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        User user = new User(
                request.email(),
                request.password(),
                request.nickname(),
                AuthProvider.EMAIL,
                UserRole.USER
        );

        userRepository.save(user);
    }

    public void login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
    }
}