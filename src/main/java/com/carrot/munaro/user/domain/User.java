package com.carrot.munaro.user.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider authProvider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    public User(
            String email,
            String password,
            String nickname,
            AuthProvider authProvider,
            UserRole role
    ) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.authProvider = authProvider;
        this.role = role;
    }
}