package com.carrot.munaro.user.repository;

import com.carrot.munaro.user.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}