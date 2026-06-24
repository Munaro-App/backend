package com.carrot.munaro.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "profiles")
public class Profile {

    public static final String DEFAULT_AVATAR_VALUE = "LION";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvatarType avatarType;

    @Column(length = 500)
    private String avatarValue;

    @Column(length = 4)
    private String mbti;

    @Column(length = 255)
    private String bio;

    public void updateProfileImage(
            AvatarType avatarType,
            String avatarValue
    ) {
        this.avatarType = avatarType;
        this.avatarValue = avatarValue;
    }

    public void resetProfileImage() {
        this.avatarType = AvatarType.PRESET;
        this.avatarValue = DEFAULT_AVATAR_VALUE;
    }
}
