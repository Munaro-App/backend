package com.carrot.munaro.user.service;

import com.carrot.munaro.global.exception.BusinessException;
import com.carrot.munaro.global.exception.ErrorCode;
import com.carrot.munaro.user.domain.Profile;
import com.carrot.munaro.user.domain.User;
import com.carrot.munaro.user.dto.response.UserResponse;
import com.carrot.munaro.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse getMe(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.USER_NOT_FOUND)
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
                .mbti(
                        profile != null
                                ? profile.getMbti()
                                : null
                )
                .bio(
                        profile != null
                                ? profile.getBio()
                                : null
                )
                .build();
    }
}