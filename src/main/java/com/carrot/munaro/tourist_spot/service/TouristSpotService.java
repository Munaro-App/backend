package com.carrot.munaro.tourist_spot.service;

import com.carrot.munaro.global.exception.BusinessException;
import com.carrot.munaro.global.exception.ErrorCode;
import com.carrot.munaro.tourist_spot.domain.TouristSpot;
import com.carrot.munaro.tourist_spot.dto.response.TouristSpotPageResponse;
import com.carrot.munaro.tourist_spot.dto.response.TouristSpotResponse;
import com.carrot.munaro.tourist_spot.repository.TouristSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TouristSpotService {

    private final TouristSpotRepository touristSpotRepository;

    @Transactional(readOnly = true)
    public TouristSpotPageResponse getNearbyTouristSpots(
            double latitude,
            double longitude,
            double radiusKm
    ) {

        List<TouristSpotResponse> content =
                touristSpotRepository
                        .findNearby(latitude, longitude, radiusKm)
                        .stream()
                        .map(TouristSpotResponse::from)
                        .toList();

        return TouristSpotPageResponse.singlePage(content);
    }

    @Transactional(readOnly = true)
    public TouristSpotResponse getTouristSpot(Long touristSpotId) {

        TouristSpot touristSpot =
                touristSpotRepository.findById(touristSpotId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.TOURIST_SPOT_NOT_FOUND
                                )
                        );

        return TouristSpotResponse.from(touristSpot);
    }
}
