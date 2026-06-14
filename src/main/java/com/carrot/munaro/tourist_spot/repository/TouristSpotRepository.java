package com.carrot.munaro.tourist_spot.repository;

import com.carrot.munaro.tourist_spot.domain.TouristSpot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TouristSpotRepository extends JpaRepository<TouristSpot, Long> {

    Page<TouristSpot> findByTouristSpotNameContainingIgnoreCaseOrAddressContainingIgnoreCase(
            String touristSpotName,
            String address,
            Pageable pageable
    );
}
