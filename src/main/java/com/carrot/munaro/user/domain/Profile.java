package com.carrot.munaro.user.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "profiles")
public class Profile {

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

    @Column(/*nullable = false,*/ length = 500)
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
        this.avatarValue = "default_avatar";
    }
}
