package com.carrot.munaro.user.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

<<<<<<< HEAD
    // OAuth 유저는 비밀번호 없을 수 있음
=======
>>>>>>> origin/main
    @Column
    private String password;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider authProvider;

    public void updateProvider(
            AuthProvider provider,
            String providerId
    ) {
        this.authProvider = provider;
        this.providerId = providerId;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider authProvider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

<<<<<<< HEAD
    // OAuth 제공자 고유 ID
=======
    @Column(nullable = false)
    @Builder.Default
    private String userStatus = "ACTIVE";

>>>>>>> origin/main
    @Column
    private String providerId;
}