package com.carrot.munaro.score.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "seasons")
public class Season {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "season_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String seasonName;

    @Column(nullable = false)
    private OffsetDateTime startedAt;

    @Column(nullable = false)
    private OffsetDateTime endedAt;

    @Column(nullable = false)
    private boolean active;

    private OffsetDateTime closedAt;

    public void close(OffsetDateTime closedAt) {
        this.active = false;
        this.closedAt = closedAt;
    }

    public boolean isExpired(OffsetDateTime now) {
        return !now.isBefore(endedAt);
    }
}
