package com.carrot.munaro.tourist_spot.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record TouristSpotPageResponse(
        List<TouristSpotResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    public static TouristSpotPageResponse from(Page<TouristSpotResponse> page) {
        return new TouristSpotPageResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    public static TouristSpotPageResponse singlePage(
            List<TouristSpotResponse> content
    ) {
        return new TouristSpotPageResponse(
                content,
                0,
                content.size(),
                content.size(),
                content.isEmpty() ? 0 : 1,
                true,
                true
        );
    }
}
