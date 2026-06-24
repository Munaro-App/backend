package com.carrot.munaro.score.repository;

import com.carrot.munaro.score.domain.Ranking;
import com.carrot.munaro.score.repository.projection.RankingRow;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RankingRepository
        extends JpaRepository<Ranking, Long> {

    boolean existsBySeasonId(Long seasonId);

    long countBySeasonId(Long seasonId);

    @Query("""
            SELECT ranking.user.id AS userId,
                   ranking.nickname AS nickname,
                   ranking.score AS score,
                   ranking.rank AS rank
            FROM Ranking ranking
            WHERE ranking.season.id = :seasonId
            ORDER BY ranking.rank ASC, ranking.user.id ASC
            """)
    List<RankingRow> findRankingRowsBySeasonId(
            @Param("seasonId") Long seasonId,
            Pageable pageable
    );

    @Query("""
            SELECT ranking.user.id AS userId,
                   ranking.nickname AS nickname,
                   ranking.score AS score,
                   ranking.rank AS rank
            FROM Ranking ranking
            WHERE ranking.season.id = :seasonId
            ORDER BY ranking.rank ASC, ranking.user.id ASC
            """)
    List<RankingRow> findRankingRowsBySeasonId(
            @Param("seasonId") Long seasonId
    );

    @Query("""
            SELECT ranking.user.id AS userId,
                   ranking.nickname AS nickname,
                   ranking.score AS score,
                   ranking.rank AS rank
            FROM Ranking ranking
            WHERE ranking.season.id = :seasonId
              AND ranking.user.id = :userId
            """)
    RankingRow findMyRankingRow(
            @Param("seasonId") Long seasonId,
            @Param("userId") Long userId
    );
}
