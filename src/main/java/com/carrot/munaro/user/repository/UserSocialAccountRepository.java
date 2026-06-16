package com.carrot.munaro.user.repository;

import com.carrot.munaro.user.domain.AuthProvider;
import com.carrot.munaro.user.domain.UserSocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSocialAccountRepository
        extends JpaRepository<UserSocialAccount, Long> {

    Optional<UserSocialAccount> findByProviderAndProviderUserId(
            AuthProvider provider,
            String providerUserId
    );

    boolean existsByProviderAndProviderUserId(
            AuthProvider provider,
            String providerUserId
    );
}
