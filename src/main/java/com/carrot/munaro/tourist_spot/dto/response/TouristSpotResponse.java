package com.carrot.munaro.tourist_spot.dto.response;

import com.carrot.munaro.tourist_spot.domain.TouristSpot;

import java.math.BigDecimal;

public record TouristSpotResponse(
        Long touristSpotId,
        String touristSpotName,
        String description,
        BigDecimal latitude,
        BigDecimal longitude,
        String sido,
        String address,
        String publicAmenityInfo,
        Integer parkingCapacity,
        Integer visitorCapacity,
        String managementPhone
) {

    public static TouristSpotResponse from(TouristSpot touristSpot) {
        return new TouristSpotResponse(
                touristSpot.getTouristSpotId(),
                touristSpot.getTouristSpotName(),
                touristSpot.getDescription(),
                touristSpot.getLatitude(),
                touristSpot.getLongitude(),
                touristSpot.getSido(),
                touristSpot.getAddress(),
                touristSpot.getPublicAmenityInfo(),
                touristSpot.getParkingCapacity(),
                touristSpot.getVisitorCapacity(),
                touristSpot.getManagementPhone()
        );
    }
}
