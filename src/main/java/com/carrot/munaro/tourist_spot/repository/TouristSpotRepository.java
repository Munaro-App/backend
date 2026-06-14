package com.carrot.munaro.tourist_spot.repository;

import com.carrot.munaro.tourist_spot.domain.TouristSpot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TouristSpotRepository extends JpaRepository<TouristSpot, Long> {
}
