package com.carrot.munaro.auth.service;

import com.carrot.munaro.auth.dto.request.EmailSignUpRequest;
import com.carrot.munaro.auth.dto.request.GoogleLoginRequest;
import com.carrot.munaro.auth.dto.request.LoginRequest;
import com.carrot.munaro.auth.dto.response.GoogleUserResponse;
import com.carrot.munaro.global.exception.BusinessException;
import com.carrot.munaro.global.exception.ErrorCode;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RestTemplate restTemplate;

    @Transactional
    public void signUp(EmailSignUpRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(
                    ErrorCode.EMAIL_ALREADY_EXISTS
            );
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .authProvider(AuthProvider.EMAIL)
                .role(UserRole.USER)
                .build();

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.INVALID_LOGIN
                        ));

        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword()
        )) {
            throw new BusinessException(
                    ErrorCode.INVALID_LOGIN
            );
        }

        String accessToken =
                jwtProvider.createToken(user.getId());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .build();
    }
    @Transactional
    public LoginResponse googleLogin(
            GoogleLoginRequest request
    ) {

        ResponseEntity<GoogleUserResponse> response =
                restTemplate.getForEntity(
                        "https://oauth2.googleapis.com/tokeninfo?id_token="
                                + request.idToken(),
                        GoogleUserResponse.class
                );

        GoogleUserResponse googleUser =
                response.getBody();

        String email =
                googleUser.getEmail();

        String nickname =
                googleUser.getName();

        String providerId =
                googleUser.getSub();

        User user = userRepository
                .findByEmail(email)
                .orElseGet(() -> {

                    User newUser = User.builder()
                            .email(email)
                            .nickname(nickname)
                            .authProvider(AuthProvider.GOOGLE)
                            .providerId(providerId)
                            .role(UserRole.USER)
                            .build();

                    return userRepository.save(newUser);
                });

        String accessToken =
                jwtProvider.createToken(user.getId());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .build();
    }
}