package com.carrot.munaro.user.repository;

import com.carrot.munaro.user.domain.UserVisitedTouristSpot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserVisitedTouristSpotRepository
        extends JpaRepository<UserVisitedTouristSpot, Long> {

    boolean existsByUser_IdAndTouristSpot_TouristSpotId(
            Long userId,
            Long touristSpotId
    );

    List<UserVisitedTouristSpot> findByUser_IdOrderByVisitedAtDesc(
            Long userId
    );

    long countByUser_Id(Long userId);
}
