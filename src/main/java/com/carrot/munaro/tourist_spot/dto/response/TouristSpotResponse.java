package com.carrot.munaro.tourist_spot.dto.response;

import com.carrot.munaro.tourist_spot.domain.TouristSpot;

public record TouristSpotResponse(

        Long touristSpotId,
        String name,
        String address,
        String category,
        String description,
        Double latitude,
        Double longitude,
        String imageUrl
) {

    public static TouristSpotResponse from(TouristSpot touristSpot) {

        return new TouristSpotResponse(
                touristSpot.getId(),
                touristSpot.getName(),
                touristSpot.getAddress(),
                touristSpot.getCategory(),
                touristSpot.getDescription(),
                touristSpot.getLatitude(),
                touristSpot.getLongitude(),
                touristSpot.getImageUrl()
        );
    }
}
