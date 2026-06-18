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

    @Column(nullable = false)
    private String seasonName;

    @Column(nullable = false)
    private OffsetDateTime startedAt;

    @Column(nullable = false)
    private OffsetDateTime endedAt;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    public void close() {
        this.active = false;
    }

    public boolean isExpired(OffsetDateTime now) {
        return !now.isBefore(endedAt);
    }

    public boolean isActive(OffsetDateTime now) {
        return active && !now.isBefore(startedAt) && now.isBefore(endedAt);
    }
}
