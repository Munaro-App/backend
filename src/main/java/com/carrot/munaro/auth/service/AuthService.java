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
import com.carrot.munaro.user.repository.UserSocialAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RestTemplate restTemplate;
    private final ProfileRepository profileRepository;
    private final UserSocialAccountRepository userSocialAccountRepository;

    @Transactional
    public void signUp(EmailSignUpRequest request) {

        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException(
                    ErrorCode.EMAIL_ALREADY_EXISTS
            );
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .nickname(ensureUniqueNickname(request.nickname()))
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

        User user = userRepository.findByEmailIgnoreCase(
                        normalizeEmail(request.email())
                )
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
                    normalizeEmail(kakaoUser.getKakao_account().getEmail());

            String nickname =
                    kakaoUser.getKakao_account()
                            .getProfile()
                            .getNickname();

            if (kakaoUser.getId() == null) {
                throw new BusinessException(
                        ErrorCode.KAKAO_USER_INFO_NOT_FOUND
                );
            }

            String providerId =
                    String.valueOf(kakaoUser.getId());

            if (nickname == null) {
                throw new BusinessException(
                        ErrorCode.KAKAO_USER_INFO_NOT_FOUND
                );
            }

            LinkedUserResult result = findOrLinkSocialUser(
                    AuthProvider.KAKAO,
                    email,
                    nickname,
                    providerId
            );

            return createLoginResponse(
                    result.user(),
                    result.isNewUser()
            );

        } catch (BusinessException e) {
            throw e;

        } catch (ResourceAccessException e) {

            e.printStackTrace();

            throw new BusinessException(
                    ErrorCode.KAKAO_SERVER_TIMEOUT
            );

        } catch (Exception e) {

            e.printStackTrace();

            throw new BusinessException(
                    ErrorCode.KAKAO_LOGIN_FAILED
            );
        }
    }

    private LinkedUserResult findOrLinkSocialUser(
            AuthProvider provider,
            String email,
            String nickname,
            String providerId
    ) {

        return userSocialAccountRepository
                .findByProviderAndProviderUserId(
                        provider,
                        providerId
                )
                .map(account -> new LinkedUserResult(
                        account.getUser(),
                        false
                ))
                .orElseGet(() -> findUserByProviderOrCreateSocialUser(
                        provider,
                        nickname,
                        providerId
                ));
    }

    private LinkedUserResult findUserByProviderOrCreateSocialUser(
            AuthProvider provider,
            String nickname,
            String providerId
    ) {

        User user = null;
        boolean isNewUser = false;

        if (providerId != null && !providerId.isBlank()) {
            user = userRepository
                    .findByAuthProviderAndProviderId(provider, providerId)
                    .orElse(null);
        }

        if (user == null) {
            user = createSocialUser(
                    provider,
                    nickname,
                    providerId
            );
            isNewUser = true;
        }

        linkSocialAccountIfAbsent(
                user,
                provider,
                providerId
        );

        return new LinkedUserResult(user, isNewUser);
    }

    private User createSocialUser(
            AuthProvider provider,
            String nickname,
            String providerId
    ) {

        User newUser = User.builder()
                .nickname(ensureUniqueNickname(nickname))
                .authProvider(provider)
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
    }

    private void linkSocialAccountIfAbsent(
            User user,
            AuthProvider provider,
            String providerId
    ) {

        if (userSocialAccountRepository
                .existsByProviderAndProviderUserId(provider, providerId)) {
            return;
        }

        userSocialAccountRepository.save(
                UserSocialAccount.builder()
                        .user(user)
                        .provider(provider)
                        .providerUserId(providerId)
                        .build()
        );
    }

    private LoginResponse createLoginResponse(
            User user,
            boolean isNewUser
    ) {

        String accessToken =
                jwtProvider.createAccessToken(user.getId());

        String refreshToken =
                jwtProvider.createRefreshToken(user.getId());

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
                                .isNewUser(isNewUser)
                                .build()
                )
                .build();
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
                    normalizeEmail(googleUser.getEmail());

            String nickname =
                    googleUser.getName();

            if (providerId == null || providerId.isBlank()) {
                throw new BusinessException(
                        ErrorCode.GOOGLE_USER_INFO_NOT_FOUND
                );
            }

            LinkedUserResult result = findOrLinkSocialUser(
                    AuthProvider.GOOGLE,
                    email,
                    nickname,
                    providerId
            );

            return createLoginResponse(
                    result.user(),
                    result.isNewUser()
            );

        } catch (BusinessException e) {
            throw e;

        } catch (Exception e) {

            e.printStackTrace();

            throw new BusinessException(
                    ErrorCode.GOOGLE_LOGIN_FAILED
            );
        }
    }

    @Transactional
    public void withdraw(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND)
                );

        user.withdraw();
    }

    private String ensureUniqueNickname(String base) {

        String cleaned =
                (base == null || base.isBlank()) ? "user" : base.trim();

        if (cleaned.length() > 16) {
            cleaned = cleaned.substring(0, 16);
        }

        if (cleaned.length() < 2) {
            cleaned = cleaned + "00";
        }

        String candidate = cleaned;

        for (int i = 0; i < 10; i++) {
            if (!userRepository.existsByNickname(candidate)) {
                return candidate;
            }

            String suffix =
                    String.valueOf(
                            ThreadLocalRandom.current()
                                    .nextInt(1000, 10000)
                    );

            int baseMaxLength = Math.max(2, 16 - suffix.length());
            String prefix = cleaned.length() > baseMaxLength
                    ? cleaned.substring(0, baseMaxLength)
                    : cleaned;

            candidate = prefix + suffix;
        }

        throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
    }

    private String normalizeEmail(String email) {

        if (email == null || email.isBlank()) {
            return null;
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private record LinkedUserResult(
            User user,
            boolean isNewUser
    ) {
    }
}
