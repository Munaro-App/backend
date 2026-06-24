package com.carrot.munaro.tourist_spot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tourist_spots")
public class TouristSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tourist_spot_id")
    private Long touristSpotId;

    @Column(name = "tourist_spot_name", nullable = false, length = 255)
    private String touristSpotName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 12, scale = 8)
    private BigDecimal longitude;

    @Column(name = "sido", length = 50)
    private String sido;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(name = "public_amenity_info", columnDefinition = "TEXT")
    private String publicAmenityInfo;

    @Column(name = "parking_capacity")
    private Integer parkingCapacity;

    @Column(name = "visitor_capacity")
    private Integer visitorCapacity;

    @Column(name = "management_phone", length = 50)
    private String managementPhone;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

}
