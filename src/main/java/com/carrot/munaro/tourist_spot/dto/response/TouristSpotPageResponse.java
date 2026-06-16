package com.carrot.munaro.tourist_spot.dto.response;

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
