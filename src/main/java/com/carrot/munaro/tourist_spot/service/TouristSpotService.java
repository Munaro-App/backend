package com.carrot.munaro.tourist_spot.service;

import com.carrot.munaro.global.exception.BusinessException;
import com.carrot.munaro.global.exception.ErrorCode;
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
                findTouristSpots(keyword, pageable)
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

    private Page<com.carrot.munaro.tourist_spot.domain.TouristSpot> findTouristSpots(
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
}
