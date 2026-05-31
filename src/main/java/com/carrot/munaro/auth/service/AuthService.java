package com.carrot.munaro.auth.service;

import com.carrot.munaro.auth.dto.request.EmailSignUpRequest;
import com.carrot.munaro.auth.dto.request.GoogleLoginRequest;
import com.carrot.munaro.auth.dto.request.KakaoLoginRequest;
import com.carrot.munaro.auth.dto.request.LoginRequest;
import com.carrot.munaro.auth.dto.response.GoogleUserResponse;
import com.carrot.munaro.auth.dto.response.KakaoUserResponse;
import com.carrot.munaro.auth.dto.response.LoginResponse;
import com.carrot.munaro.global.exception.BusinessException;
import com.carrot.munaro.global.exception.ErrorCode;
import com.carrot.munaro.security.JwtProvider;
import com.carrot.munaro.user.domain.AuthProvider;
import com.carrot.munaro.user.domain.User;
import com.carrot.munaro.user.domain.UserRole;
import com.carrot.munaro.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
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
                .profileImageUrl(null)
                .authProvider(AuthProvider.EMAIL)
                .role(UserRole.USER)
                .userStatus("ACTIVE")
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
                .refreshToken(null)
                .expiresIn(3600L)
                .user(
                        LoginResponse.UserInfo.builder()
                                .id(user.getId())
                                .nickname(user.getNickname())
                                .profileImageUrl(user.getProfileImageUrl())
                                .userRole(user.getRole().name())
                                .userStatus(user.getUserStatus())
                                .isNewUser(false)
                                .dogSetupRequired(false)
                                .build()
                )
                .build();
    }

    @Transactional
    public LoginResponse kakaoLogin(
            KakaoLoginRequest request
    ) {

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(request.accessToken());

            HttpEntity<Void> entity =
                    new HttpEntity<>(headers);

            ResponseEntity<KakaoUserResponse> response =
                    restTemplate.exchange(
                            "https://kapi.kakao.com/v2/user/me",
                            HttpMethod.GET,
                            entity,
                            KakaoUserResponse.class
                    );

            KakaoUserResponse kakaoUser =
                    response.getBody();

            if (kakaoUser == null ||
                    kakaoUser.getKakao_account() == null ||
                    kakaoUser.getKakao_account().getProfile() == null) {

                throw new BusinessException(
                        ErrorCode.KAKAO_USER_INFO_NOT_FOUND
                );
            }

            String email =
                    kakaoUser.getKakao_account().getEmail();

            String nickname =
                    kakaoUser.getKakao_account()
                            .getProfile()
                            .getNickname();

            String providerId =
                    String.valueOf(kakaoUser.getId());

            if (email == null || nickname == null) {
                throw new BusinessException(
                        ErrorCode.KAKAO_USER_INFO_NOT_FOUND
                );
            }

            User user = userRepository
                    .findByEmail(email)
                    .map(existingUser -> {

                        if (existingUser.getProviderId() == null) {
                            existingUser.updateProvider(
                                    AuthProvider.KAKAO,
                                    providerId
                            );

                            return userRepository.save(existingUser);
                        }

                        return existingUser;
                    })
                    .orElseGet(() -> {

                        User newUser = User.builder()
                                .email(email)
                                .nickname(nickname)
                                .profileImageUrl(null)
                                .authProvider(AuthProvider.KAKAO)
                                .providerId(providerId)
                                .role(UserRole.USER)
                                .userStatus("ACTIVE")
                                .build();

                        return userRepository.save(newUser);
                    });

            String accessToken =
                    jwtProvider.createToken(user.getId());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(null)
                    .expiresIn(3600L)
                    .user(
                            LoginResponse.UserInfo.builder()
                                    .id(user.getId())
                                    .nickname(user.getNickname())
                                    .profileImageUrl(
                                            user.getProfileImageUrl()
                                    )
                                    .userRole(
                                            user.getRole().name()
                                    )
                                    .userStatus(
                                            user.getUserStatus()
                                    )
                                    .isNewUser(false)
                                    .dogSetupRequired(false)
                                    .build()
                    )
                    .build();

        } catch (ResourceAccessException e) {

            throw new BusinessException(
                    ErrorCode.KAKAO_SERVER_TIMEOUT
            );

        } catch (Exception e) {

            throw new BusinessException(
                    ErrorCode.KAKAO_LOGIN_FAILED
            );
        }
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

        if (googleUser == null) {
            throw new BusinessException(
                    ErrorCode.GOOGLE_LOGIN_FAILED
            );
        }

        String email = googleUser.getEmail();
        String nickname = googleUser.getName();
        String providerId = googleUser.getSub();

        User user = userRepository
                .findByEmail(email)
                .map(existingUser -> {

                    if (existingUser.getProviderId() == null) {
                        existingUser.updateProvider(
                                AuthProvider.GOOGLE,
                                providerId
                        );

                        return userRepository.save(existingUser);
                    }

                    return existingUser;
                })
                .orElseGet(() -> {

                    User newUser = User.builder()
                            .email(email)
                            .nickname(nickname)
                            .profileImageUrl(null)
                            .authProvider(AuthProvider.GOOGLE)
                            .providerId(providerId)
                            .role(UserRole.USER)
                            .userStatus("ACTIVE")
                            .build();

                    return userRepository.save(newUser);
                });

        String accessToken =
                jwtProvider.createToken(user.getId());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(null)
                .expiresIn(3600L)
                .user(
                        LoginResponse.UserInfo.builder()
                                .id(user.getId())
                                .nickname(user.getNickname())
                                .profileImageUrl(
                                        user.getProfileImageUrl()
                                )
                                .userRole(
                                        user.getRole().name()
                                )
                                .userStatus(
                                        user.getUserStatus()
                                )
                                .isNewUser(false)
                                .dogSetupRequired(false)
                                .build()
                )
                .build();
    }
}