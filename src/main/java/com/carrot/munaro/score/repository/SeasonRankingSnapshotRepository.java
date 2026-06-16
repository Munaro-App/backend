package com.carrot.munaro.score.repository;

import com.carrot.munaro.score.domain.SeasonRankingSnapshot;
import com.carrot.munaro.score.repository.projection.RankingRow;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeasonRankingSnapshotRepository
        extends JpaRepository<SeasonRankingSnapshot, Long> {

    boolean existsBySeasonId(Long seasonId);

    long countBySeasonId(Long seasonId);

    @Query("""
            SELECT snapshot.user.id AS userId,
                   snapshot.nickname AS nickname,
                   snapshot.score AS score,
                   snapshot.rank AS rank
            FROM SeasonRankingSnapshot snapshot
            WHERE snapshot.season.id = :seasonId
            ORDER BY snapshot.rank ASC, snapshot.user.id ASC
            """)
    List<RankingRow> findRankingRowsBySeasonId(
            @Param("seasonId") Long seasonId,
            Pageable pageable
    );

    @Query("""
            SELECT snapshot.user.id AS userId,
                   snapshot.nickname AS nickname,
                   snapshot.score AS score,
                   snapshot.rank AS rank
            FROM SeasonRankingSnapshot snapshot
            WHERE snapshot.season.id = :seasonId
              AND snapshot.user.id = :userId
            """)
    RankingRow findMyRankingRow(
            @Param("seasonId") Long seasonId,
            @Param("userId") Long userId
    );
}
