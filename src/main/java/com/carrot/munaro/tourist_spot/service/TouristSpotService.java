package com.carrot.munaro.tourist_spot.service;

import com.carrot.munaro.global.exception.BusinessException;
import com.carrot.munaro.global.exception.ErrorCode;
import com.carrot.munaro.tourist_spot.domain.TouristSpot;
import com.carrot.munaro.tourist_spot.dto.response.TouristSpotPageResponse;
import com.carrot.munaro.tourist_spot.dto.response.TouristSpotResponse;
import com.carrot.munaro.tourist_spot.repository.TouristSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TouristSpotService {

    private final TouristSpotRepository touristSpotRepository;

    public TouristSpotPageResponse getTouristSpots(
            String keyword,
            Pageable pageable
    ) {
        Page<TouristSpotResponse> touristSpots =
                findTouristSpots(normalizeKeyword(keyword), pageable)
                        .map(TouristSpotResponse::from);

        return TouristSpotPageResponse.from(touristSpots);
    }

    public TouristSpotResponse getTouristSpot(Long id) {
        return touristSpotRepository.findById(id)
                .map(TouristSpotResponse::from)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.TOURIST_SPOT_NOT_FOUND
                        )
                );
    }

    public TouristSpotPageResponse getNearbyTouristSpots(
            Double latitude,
            Double longitude,
            Double radiusKm,
            Pageable pageable
    ) {
        validateLocation(latitude, longitude, radiusKm);

        Page<TouristSpotResponse> touristSpots =
                touristSpotRepository
                        .findNearby(latitude, longitude, radiusKm, pageable)
                        .map(TouristSpotResponse::from);

        return TouristSpotPageResponse.from(touristSpots);
    }

    private Page<TouristSpot> findTouristSpots(
            String keyword,
            Pageable pageable
    ) {
        if (keyword == null || keyword.isBlank()) {
            return touristSpotRepository.findAll(pageable);
        }

        return touristSpotRepository
                .findByTouristSpotNameContainingIgnoreCaseOrAddressContainingIgnoreCase(
                        keyword,
                        keyword,
                        pageable
                );
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        return keyword.trim();
    }

    private void validateLocation(
            Double latitude,
            Double longitude,
            Double radiusKm
    ) {
        if (latitude == null || longitude == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        if (latitude < -90 || latitude > 90) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        if (longitude < -180 || longitude > 180) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        if (radiusKm == null || radiusKm <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }
}
