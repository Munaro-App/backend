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
import com.carrot.munaro.user.domain.AvatarType;
import com.carrot.munaro.user.domain.Profile;
import com.carrot.munaro.user.domain.User;
import com.carrot.munaro.user.domain.UserRole;
import com.carrot.munaro.user.domain.UserSocialAccount;
import com.carrot.munaro.user.domain.UserStatus;
import com.carrot.munaro.user.repository.ProfileRepository;
import com.carrot.munaro.user.repository.UserRepository;
import com.carrot.munaro.user.repository.UserSocialAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .nickname(ensureUniqueNickname(request.nickname()))
                .authProvider(AuthProvider.EMAIL)
                .role(UserRole.USER)
                .userStatus(UserStatus.ACTIVE)
                .build();

        createDefaultProfile(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmailIgnoreCase(
                        normalizeEmail(request.email())
                )
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.INVALID_LOGIN)
                );

        validateActiveUser(user);

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN);
        }

        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword()
        )) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN);
        }

        return createLoginResponse(user, false);
    }

    @Transactional
    public LoginResponse kakaoLogin(KakaoLoginRequest request) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(request.accessToken());

            ResponseEntity<KakaoUserResponse> response =
                    restTemplate.exchange(
                            "https://kapi.kakao.com/v2/user/me",
                            HttpMethod.GET,
                            new HttpEntity<Void>(headers),
                            KakaoUserResponse.class
                    );

            KakaoUserResponse kakaoUser = response.getBody();

            if (kakaoUser == null
                    || kakaoUser.getId() == null
                    || kakaoUser.getKakao_account() == null
                    || kakaoUser.getKakao_account().getProfile() == null) {
                throw new BusinessException(
                        ErrorCode.KAKAO_USER_INFO_NOT_FOUND
                );
            }

            String providerId = String.valueOf(kakaoUser.getId());
            String nickname = kakaoUser.getKakao_account()
                    .getProfile()
                    .getNickname();

            if (nickname == null || nickname.isBlank()) {
                throw new BusinessException(
                        ErrorCode.KAKAO_USER_INFO_NOT_FOUND
                );
            }

            LinkedUserResult result = findOrCreateSocialUser(
                    AuthProvider.KAKAO,
                    nickname,
                    providerId
            );

            return createLoginResponse(result.user(), result.isNewUser());
        } catch (BusinessException e) {
            throw e;
        } catch (ResourceAccessException e) {
            throw new BusinessException(ErrorCode.KAKAO_SERVER_TIMEOUT);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.KAKAO_LOGIN_FAILED);
        }
    }

    @Transactional
    public LoginResponse googleLogin(GoogleLoginRequest request) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(request.accessToken());

            ResponseEntity<GoogleUserResponse> response =
                    restTemplate.exchange(
                            "https://www.googleapis.com/oauth2/v3/userinfo",
                            HttpMethod.GET,
                            new HttpEntity<Void>(headers),
                            GoogleUserResponse.class
                    );

            GoogleUserResponse googleUser = response.getBody();

            if (googleUser == null
                    || googleUser.getSub() == null
                    || googleUser.getSub().isBlank()) {
                throw new BusinessException(
                        ErrorCode.GOOGLE_USER_INFO_NOT_FOUND
                );
            }

            LinkedUserResult result = findOrCreateSocialUser(
                    AuthProvider.GOOGLE,
                    googleUser.getName(),
                    googleUser.getSub()
            );

            return createLoginResponse(result.user(), result.isNewUser());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.GOOGLE_LOGIN_FAILED);
        }
    }

    @Transactional
    public void withdraw(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND)
                );

        user.withdraw("deleted_user_" + user.getId());
    }

    private LinkedUserResult findOrCreateSocialUser(
            AuthProvider provider,
            String nickname,
            String providerId
    ) {

        return userSocialAccountRepository
                .findByProviderAndProviderUserId(provider, providerId)
                .map(account -> {
                    validateActiveUser(account.getUser());
                    return new LinkedUserResult(account.getUser(), false);
                })
                .orElseGet(() -> createSocialUserWithAccount(
                        provider,
                        nickname,
                        providerId
                ));
    }

    private LinkedUserResult createSocialUserWithAccount(
            AuthProvider provider,
            String nickname,
            String providerId
    ) {

        User user = userRepository
                .findByAuthProviderAndProviderId(provider, providerId)
                .orElseGet(() -> {
                    User savedUser = userRepository.save(
                            User.builder()
                                    .nickname(ensureUniqueNickname(nickname))
                                    .authProvider(provider)
                                    .providerId(providerId)
                                    .role(UserRole.USER)
                                    .userStatus(UserStatus.ACTIVE)
                                    .build()
                    );

                    createDefaultProfile(savedUser);

                    return savedUser;
                });

        validateActiveUser(user);
        linkSocialAccountIfAbsent(user, provider, providerId);

        return new LinkedUserResult(user, true);
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

    private void createDefaultProfile(User user) {

        profileRepository.save(
                Profile.builder()
                        .user(user)
                        .avatarType(AvatarType.PRESET)
                        .avatarValue(Profile.DEFAULT_AVATAR_VALUE)
                        .build()
        );
    }

    private LoginResponse createLoginResponse(
            User user,
            boolean isNewUser
    ) {

        return LoginResponse.builder()
                .accessToken(jwtProvider.createAccessToken(user.getId()))
                .refreshToken(jwtProvider.createRefreshToken(user.getId()))
                .expiresIn(3600L)
                .user(
                        LoginResponse.UserInfo.builder()
                                .id(user.getId())
                                .nickname(user.getNickname())
                                .userRole(user.getRole().name())
                                .userStatus(user.getUserStatus().name())
                                .isNewUser(isNewUser)
                                .dogSetupRequired(false)
                                .build()
                )
                .build();
    }

    private void validateActiveUser(User user) {

        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN);
        }
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

            String suffix = String.valueOf(
                    ThreadLocalRandom.current().nextInt(1000, 10000)
            );
            int prefixMaxLength = Math.max(2, 16 - suffix.length());
            String prefix = cleaned.length() > prefixMaxLength
                    ? cleaned.substring(0, prefixMaxLength)
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
