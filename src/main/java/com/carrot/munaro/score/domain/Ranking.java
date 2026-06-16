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
        name = "rankings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_rankings_season_user",
                        columnNames = {"season_id", "user_id"}
                )
        }
)
public class Ranking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ranking_id")
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

    @Column(name = "rank", nullable = false)
    private Integer rank;

    @Column(nullable = false)
    private OffsetDateTime snapshottedAt;
}
