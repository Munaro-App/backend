package com.carrot.munaro.user.repository;

import com.carrot.munaro.user.domain.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    List<UserBadge> findTop3ByUser_IdOrderByEarnedAtDesc(Long userId);
}
