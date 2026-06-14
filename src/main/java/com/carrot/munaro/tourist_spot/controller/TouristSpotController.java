package com.carrot.munaro.tourist_spot.controller;

import com.carrot.munaro.global.response.ApiResponse;
import com.carrot.munaro.tourist_spot.dto.response.TouristSpotPageResponse;
import com.carrot.munaro.tourist_spot.dto.response.TouristSpotResponse;
import com.carrot.munaro.tourist_spot.service.KakaoLocalClient;
import com.carrot.munaro.tourist_spot.service.TouristSpotService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tourist-spots")
@RequiredArgsConstructor
public class TouristSpotController {

    private final KakaoLocalClient kakaoLocalClient;
    private final TouristSpotService touristSpotService;

    @GetMapping
    public ApiResponse<TouristSpotPageResponse> getTouristSpots(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by("id").ascending()
                );

        return ApiResponse.ok(
                touristSpotService.getTouristSpots(
                        keyword,
                        pageable
                )
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<TouristSpotResponse> getTouristSpot(
            @PathVariable Long id
    ) {

        return ApiResponse.ok(
                touristSpotService.getTouristSpot(id)
        );
    }

    @GetMapping("/search")
    public String search(
            @RequestParam String keyword
    ) {
        return kakaoLocalClient.searchPlace(keyword);
    }
}
