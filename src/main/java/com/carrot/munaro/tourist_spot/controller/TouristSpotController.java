package com.carrot.munaro.tourist_spot.controller;

import com.carrot.munaro.global.response.ApiResponse;
import com.carrot.munaro.tourist_spot.dto.response.TouristSpotPageResponse;
import com.carrot.munaro.tourist_spot.dto.response.TouristSpotResponse;
import com.carrot.munaro.tourist_spot.service.TouristSpotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tourist-spots")
@RequiredArgsConstructor
public class TouristSpotController {

    private final TouristSpotService touristSpotService;

    @GetMapping("/nearby")
    public ApiResponse<TouristSpotPageResponse> getNearbyTouristSpots(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "10") double radiusKm
    ) {

        return ApiResponse.ok(
                touristSpotService.getNearbyTouristSpots(
                        latitude,
                        longitude,
                        radiusKm
                )
        );
    }

    @GetMapping("/{spotId}")
    public ApiResponse<TouristSpotResponse> getTouristSpot(
            @PathVariable Long spotId
    ) {

        return ApiResponse.ok(
                touristSpotService.getTouristSpot(spotId)
        );
    }
}
