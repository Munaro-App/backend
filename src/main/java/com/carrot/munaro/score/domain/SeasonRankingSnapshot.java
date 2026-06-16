package com.carrot.munaro.score.domain;

import com.carrot.munaro.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "season_ranking_snapshots",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_season_ranking_snapshot_season_user",
                        columnNames = {"season_id", "user_id"}
                )
        }
)
public class SeasonRankingSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "season_ranking_snapshot_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private Integer score;

    @Column(name = "ranking", nullable = false)
    private Integer rank;

    @Column(nullable = false)
    private OffsetDateTime snapshottedAt;
}
