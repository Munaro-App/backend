package com.carrot.munaro.tourist_spot.domain;

import jakarta.persistence.*;
import lombok.*;

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
    private Long id;

    @Column(length = 100)
    private String name;

    @Column(length = 500)
    private String address;

    @Column(length = 50)
    private String category;

    @Column(length = 2000)
    private String description;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(length = 500)
    private String imageUrl;
}
