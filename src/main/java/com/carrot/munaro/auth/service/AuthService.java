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
import com.carrot.munaro.user.domain.*;
import com.carrot.munaro.user.repository.ProfileRepository;
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
    private final ProfileRepository profileRepository;

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
                .userStatus(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        Profile profile = Profile.builder()
                .user(savedUser)
                .avatarType(AvatarType.PRESET)
                .avatarValue("default_avatar")
                .build();

        profileRepository.save(profile);
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
                jwtProvider.createAccessToken(user.getId());

        String refreshToken =
                jwtProvider.createRefreshToken(user.getId());

        user.updateRefreshToken(refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(3600L)
                .user(
                        LoginResponse.UserInfo.builder()
                                .id(user.getId())
                                .nickname(user.getNickname())
                                .userRole(user.getRole().name())
                                .userStatus(user.getUserStatus().name())
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

            System.out.println("===== 카카오 유저 =====");
            System.out.println("email = " + email);
            System.out.println("nickname = " + nickname);
            System.out.println("providerId = " + providerId);
            System.out.println("======================");

            if (nickname == null) {
                throw new BusinessException(
                        ErrorCode.KAKAO_USER_INFO_NOT_FOUND
                );
            }

            User user;

            // 1. 이메일이 있으면 기존 이메일 계정 연동
            if (email != null) {

                user = userRepository
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
                        .orElse(null);

                if (user != null) {

                    String accessToken =
                            jwtProvider.createAccessToken(user.getId());

                    String refreshToken =
                            jwtProvider.createRefreshToken(user.getId());

                    user.updateRefreshToken(refreshToken);

                    return LoginResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .expiresIn(3600L)
                            .user(
                                    LoginResponse.UserInfo.builder()
                                            .id(user.getId())
                                            .nickname(user.getNickname())
                                            .userRole(
                                                    user.getRole().name()
                                            )
                                            .userStatus(
                                                    user.getUserStatus().name()
                                            )
                                            .isNewUser(false)
                                            .dogSetupRequired(false)
                                            .build()
                            )
                            .build();
                }
            }

            // 2. providerId로 조회
            user = userRepository
                    .findByProviderId(providerId)
                    .orElseGet(() -> {

                        User newUser = User.builder()
                                .email(email)
                                .nickname(nickname)
                                .authProvider(AuthProvider.KAKAO)
                                .providerId(providerId)
                                .role(UserRole.USER)
                                .userStatus(UserStatus.ACTIVE)
                                .build();

                        User savedUser = userRepository.save(newUser);

                        Profile profile = Profile.builder()
                                .user(savedUser)
                                .avatarType(AvatarType.PRESET)
                                .avatarValue("default_avatar")
                                .build();

                        profileRepository.save(profile);

                        return savedUser;
                    });

            String accessToken =
                    jwtProvider.createAccessToken(user.getId());

            String refreshToken =
                    jwtProvider.createRefreshToken(user.getId());

            user.updateRefreshToken(refreshToken);

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(3600L)
                    .user(
                            LoginResponse.UserInfo.builder()
                                    .id(user.getId())
                                    .nickname(user.getNickname())
                                    .userRole(
                                            user.getRole().name()
                                    )
                                    .userStatus(
                                            user.getUserStatus().name()
                                    )
                                    .isNewUser(false)
                                    .dogSetupRequired(false)
                                    .build()
                    )
                    .build();

        } catch (ResourceAccessException e) {

            e.printStackTrace();

            throw new BusinessException(
                    ErrorCode.KAKAO_SERVER_TIMEOUT
            );

        } catch (Exception e) {

            e.printStackTrace();

            throw e;
        }
    }

    @Transactional
    public LoginResponse googleLogin(
            GoogleLoginRequest request
    ) {

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(request.accessToken());

            HttpEntity<Void> entity =
                    new HttpEntity<>(headers);

            ResponseEntity<GoogleUserResponse> response =
                    restTemplate.exchange(
                            "https://www.googleapis.com/oauth2/v3/userinfo",
                            HttpMethod.GET,
                            entity,
                            GoogleUserResponse.class
                    );

            GoogleUserResponse googleUser =
                    response.getBody();

            if (googleUser == null) {
                throw new BusinessException(
                        ErrorCode.GOOGLE_USER_INFO_NOT_FOUND
                );
            }

            String providerId =
                    googleUser.getSub();

            String email =
                    googleUser.getEmail();

            String nickname =
                    googleUser.getName();

            User user =
                    userRepository
                            .findByProviderId(providerId)
                            .orElseGet(() -> {

                                User newUser =
                                        User.builder()
                                                .email(email)
                                                .nickname(nickname)
                                                .authProvider(AuthProvider.GOOGLE)
                                                .providerId(providerId)
                                                .role(UserRole.USER)
                                                .userStatus(UserStatus.ACTIVE)
                                                .build();

                                User savedUser = userRepository.save(newUser);

                                Profile profile = Profile.builder()
                                        .user(savedUser)
                                        .avatarType(AvatarType.PRESET)
                                        .avatarValue("default_avatar")
                                        .build();

                                profileRepository.save(profile);

                                return savedUser;
                            });

            String accessToken =
                    jwtProvider.createAccessToken(user.getId());

            String refreshToken =
                    jwtProvider.createRefreshToken(user.getId());

            user.updateRefreshToken(refreshToken);

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(3600L)
                    .user(
                            LoginResponse.UserInfo.builder()
                                    .id(user.getId())
                                    .nickname(user.getNickname())
                                    .userRole(
                                            user.getRole().name()
                                    )
                                    .userStatus(
                                            user.getUserStatus().name()
                                    )
                                    .isNewUser(false)
                                    .dogSetupRequired(false)
                                    .build()
                    )
                    .build();

        } catch (Exception e) {

            e.printStackTrace();

            throw new BusinessException(
                    ErrorCode.GOOGLE_LOGIN_FAILED
            );
        }
    }

}