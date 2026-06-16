package com.carrot.munaro.user.repository;

import com.carrot.munaro.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByProviderId(String providerId);

    boolean existsByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);
}
