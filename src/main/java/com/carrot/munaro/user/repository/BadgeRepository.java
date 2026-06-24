package com.carrot.munaro.user.repository;

import com.carrot.munaro.user.domain.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    List<Badge> findByBadgeType(String badgeType);
}
