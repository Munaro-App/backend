package com.carrot.munaro.user.service;

import com.carrot.munaro.global.exception.BusinessException;
import com.carrot.munaro.global.exception.ErrorCode;
import com.carrot.munaro.user.domain.AvatarType;
import com.carrot.munaro.user.domain.PresetAvatar;
import com.carrot.munaro.user.domain.Profile;
import com.carrot.munaro.user.domain.User;
import com.carrot.munaro.user.dto.request.ProfileUpdateRequest;
import com.carrot.munaro.user.dto.response.ProfileUpdateResponse;
import com.carrot.munaro.user.dto.response.UserResponse;
import com.carrot.munaro.user.repository.ProfileRepository;
import com.carrot.munaro.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    @Transactional(readOnly = true)
    public UserResponse getMe(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND)
                );

        Profile profile = user.getProfile();

        return UserResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatarValue(
                        profile != null
                                ? profile.getAvatarValue()
                                : null
                )
                .mbti(profile != null ? profile.getMbti() : null)
                .bio(profile != null ? profile.getBio() : null)
                .build();
    }

    @Transactional
    public ProfileUpdateResponse updateProfile(
            Long userId,
            ProfileUpdateRequest request
    ) {

        User user = getUser(userId);
        Profile profile = getOrCreateProfile(user);
        boolean changed = false;

        if (request.nickname() != null) {
            updateNickname(user, request.nickname());
            changed = true;
        }

        if (request.avatarType() != null || request.avatarValue() != null) {
            updateProfileImage(
                    profile,
                    request.avatarType(),
                    request.avatarValue()
            );
            changed = true;
        }

        if (!changed) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        return new ProfileUpdateResponse(
                user.getId(),
                user.getNickname(),
                profile.getAvatarType(),
                profile.getAvatarValue()
        );
    }

    private User getUser(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND)
                );
    }

    private Profile getOrCreateProfile(User user) {

        return profileRepository.findByUserId(user.getId())
                .orElseGet(() ->
                        profileRepository.save(
                                Profile.builder()
                                        .user(user)
                                        .avatarType(AvatarType.PRESET)
                                        .avatarValue(
                                                Profile.DEFAULT_AVATAR_VALUE
                                        )
                                        .build()
                        )
                );
    }

    private void updateNickname(User user, String nicknameValue) {

        String nickname = nicknameValue.trim();

        if (nickname.length() < 2 || nickname.length() > 16) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        if (!user.getNickname().equals(nickname)
                && userRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        user.updateNickname(nickname);
    }

    private void updateProfileImage(
            Profile profile,
            AvatarType avatarType,
            String avatarValue
    ) {

        validateProfileImage(avatarType, avatarValue);
        profile.updateProfileImage(avatarType, avatarValue.trim());
    }

    private void validateProfileImage(
            AvatarType avatarType,
            String avatarValue
    ) {

        if (avatarType == null
                || avatarType != AvatarType.PRESET
                || avatarValue == null
                || avatarValue.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        if (!PresetAvatar.contains(avatarValue)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }
}
