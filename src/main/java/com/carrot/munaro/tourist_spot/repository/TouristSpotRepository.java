package com.carrot.munaro.tourist_spot.repository;

import com.carrot.munaro.tourist_spot.domain.TouristSpot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TouristSpotRepository extends JpaRepository<TouristSpot, Long> {

    Page<TouristSpot> findByTouristSpotNameContainingIgnoreCaseOrAddressContainingIgnoreCase(
            String touristSpotName,
            String address,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT *
                    FROM tourist_spots
                    WHERE latitude IS NOT NULL
                      AND longitude IS NOT NULL
                      AND (
                          6371 * ACOS(
                              LEAST(
                                  1,
                                  GREATEST(
                                      -1,
                                      COS(RADIANS(:latitude))
                                      * COS(RADIANS(CAST(latitude AS DOUBLE PRECISION)))
                                      * COS(RADIANS(CAST(longitude AS DOUBLE PRECISION)) - RADIANS(:longitude))
                                      + SIN(RADIANS(:latitude))
                                      * SIN(RADIANS(CAST(latitude AS DOUBLE PRECISION)))
                                  )
                              )
                          )
                      ) <= :radiusKm
                    ORDER BY (
                        6371 * ACOS(
                            LEAST(
                                1,
                                GREATEST(
                                    -1,
                                    COS(RADIANS(:latitude))
                                    * COS(RADIANS(CAST(latitude AS DOUBLE PRECISION)))
                                    * COS(RADIANS(CAST(longitude AS DOUBLE PRECISION)) - RADIANS(:longitude))
                                    + SIN(RADIANS(:latitude))
                                    * SIN(RADIANS(CAST(latitude AS DOUBLE PRECISION)))
                                )
                            )
                        )
                    ) ASC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM tourist_spots
                    WHERE latitude IS NOT NULL
                      AND longitude IS NOT NULL
                      AND (
                          6371 * ACOS(
                              LEAST(
                                  1,
                                  GREATEST(
                                      -1,
                                      COS(RADIANS(:latitude))
                                      * COS(RADIANS(CAST(latitude AS DOUBLE PRECISION)))
                                      * COS(RADIANS(CAST(longitude AS DOUBLE PRECISION)) - RADIANS(:longitude))
                                      + SIN(RADIANS(:latitude))
                                      * SIN(RADIANS(CAST(latitude AS DOUBLE PRECISION)))
                                  )
                              )
                          )
                      ) <= :radiusKm
                    """,
            nativeQuery = true
    )
    Page<TouristSpot> findNearby(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusKm") Double radiusKm,
            Pageable pageable
    );
}
