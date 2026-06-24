package com.carrot.munaro.score.repository;

import com.carrot.munaro.score.domain.Score;
import com.carrot.munaro.score.repository.projection.RankingRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScoreRepository
        extends JpaRepository<Score, Long> {

    @Query(
            value = """
                    SELECT
                        ranked.user_id AS "userId",
                        ranked.nickname AS "nickname",
                        ranked.score AS "score",
                        ranked.ranking AS "rank"
                    FROM (
                        SELECT
                            u.user_id,
                            u.nickname,
                            COALESCE(SUM(s.points), 0)::int AS score,
                            RANK() OVER (
                                ORDER BY COALESCE(SUM(s.points), 0) DESC, u.user_id ASC
                            )::int AS ranking
                        FROM scores s
                        JOIN users u ON u.user_id = s.user_id
                        WHERE s.season_id = :seasonId
                        GROUP BY u.user_id, u.nickname
                    ) ranked
                    ORDER BY ranked.ranking ASC, ranked.user_id ASC
                    LIMIT :limit OFFSET :offset
                    """,
            nativeQuery = true
    )
    List<RankingRow> findRankingRows(
            @Param("seasonId") Long seasonId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Query(
            value = """
                    SELECT
                        ranked.user_id AS "userId",
                        ranked.nickname AS "nickname",
                        ranked.score AS "score",
                        ranked.ranking AS "rank"
                    FROM (
                        SELECT
                            u.user_id,
                            u.nickname,
                            COALESCE(SUM(s.points), 0)::int AS score,
                            RANK() OVER (
                                ORDER BY COALESCE(SUM(s.points), 0) DESC, u.user_id ASC
                            )::int AS ranking
                        FROM scores s
                        JOIN users u ON u.user_id = s.user_id
                        WHERE s.season_id = :seasonId
                        GROUP BY u.user_id, u.nickname
                    ) ranked
                    ORDER BY ranked.ranking ASC, ranked.user_id ASC
                    """,
            nativeQuery = true
    )
    List<RankingRow> findRankingRows(
            @Param("seasonId") Long seasonId
    );

    @Query(
            value = """
                    SELECT COUNT(*)
                    FROM (
                        SELECT s.user_id
                        FROM scores s
                        WHERE s.season_id = :seasonId
                        GROUP BY s.user_id
                    ) ranked
                    """,
            nativeQuery = true
    )
    long countRankingUsers(@Param("seasonId") Long seasonId);

    @Query(
            value = """
                    SELECT
                        ranked.user_id AS "userId",
                        ranked.nickname AS "nickname",
                        ranked.score AS "score",
                        ranked.ranking AS "rank"
                    FROM (
                        SELECT
                            u.user_id,
                            u.nickname,
                            COALESCE(SUM(s.points), 0)::int AS score,
                            RANK() OVER (
                                ORDER BY COALESCE(SUM(s.points), 0) DESC, u.user_id ASC
                            )::int AS ranking
                        FROM scores s
                        JOIN users u ON u.user_id = s.user_id
                        WHERE s.season_id = :seasonId
                        GROUP BY u.user_id, u.nickname
                    ) ranked
                    WHERE ranked.user_id = :userId
                    """,
            nativeQuery = true
    )
    RankingRow findMyRankingRow(
            @Param("seasonId") Long seasonId,
            @Param("userId") Long userId
    );

    @Query("""
            SELECT COALESCE(SUM(score.points), 0)
            FROM Score score
            WHERE score.user.id = :userId
            """)
    Integer sumPointsByUserId(@Param("userId") Long userId);
}
