package com.carrot.munaro.tourist_spot.repository;

import com.carrot.munaro.tourist_spot.domain.TouristSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TouristSpotRepository
        extends JpaRepository<TouristSpot, Long> {

    @Query(
            value = """
                    select *
                    from tourist_spots
                    where latitude is not null
                      and longitude is not null
                      and (
                          6371 * acos(
                              least(1, greatest(-1,
                                  cos(radians(:latitude))
                                  * cos(radians(latitude))
                                  * cos(radians(longitude) - radians(:longitude))
                                  + sin(radians(:latitude))
                                  * sin(radians(latitude))
                              ))
                          )
                      ) <= :radiusKm
                    order by (
                        6371 * acos(
                            least(1, greatest(-1,
                                cos(radians(:latitude))
                                * cos(radians(latitude))
                                * cos(radians(longitude) - radians(:longitude))
                                + sin(radians(:latitude))
                                * sin(radians(latitude))
                            ))
                        )
                    ) asc
                    limit 100
                    """,
            nativeQuery = true
    )
    List<TouristSpot> findNearby(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radiusKm") double radiusKm
    );
}
