package com.carrot.munaro.auth.service;

import com.carrot.munaro.auth.dto.request.EmailSignUpRequest;
import com.carrot.munaro.auth.dto.request.LoginRequest;
import com.carrot.munaro.security.JwtProvider;
import com.carrot.munaro.user.domain.AuthProvider;
import com.carrot.munaro.user.domain.User;
import com.carrot.munaro.user.domain.UserRole;
import com.carrot.munaro.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.carrot.munaro.auth.dto.response.LoginResponse;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public void signUp(EmailSignUpRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        User user = new User(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.nickname(),
                AuthProvider.EMAIL,
                UserRole.USER
        );

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("사용자 없음"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new RuntimeException("비밀번호 불일치");
        }

        String accessToken =
                jwtProvider.createToken(user.getId());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .build();
    }}