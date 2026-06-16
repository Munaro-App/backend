package com.carrot.munaro.score.service;

import com.carrot.munaro.score.domain.Ranking;
import com.carrot.munaro.score.domain.Season;
import com.carrot.munaro.score.domain.SeasonWinner;
import com.carrot.munaro.score.repository.RankingRepository;
import com.carrot.munaro.score.repository.ScoreRepository;
import com.carrot.munaro.score.repository.SeasonWinnerRepository;
import com.carrot.munaro.score.repository.projection.RankingRow;
import com.carrot.munaro.user.domain.User;
import com.carrot.munaro.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingSnapshotService {

    private static final int SNAPSHOT_BATCH_SIZE = 1000;

    private final ScoreRepository scoreRepository;
    private final UserRepository userRepository;
    private final RankingRepository rankingRepository;
    private final SeasonWinnerRepository winnerRepository;

    @Transactional
    public void snapshotSeason(
            Season season,
            OffsetDateTime snapshottedAt
    ) {

        if (rankingRepository.existsBySeasonId(season.getId())) {
            return;
        }

        int offset = 0;
        boolean winnerSaved = false;

        while (true) {
            List<RankingRow> rankingRows =
                    scoreRepository.findRankingRows(
                            season.getId(),
                            SNAPSHOT_BATCH_SIZE,
                            offset
                    );

            if (rankingRows.isEmpty()) {
                break;
            }

            if (!winnerSaved) {
                saveWinner(season, rankingRows, snapshottedAt);
                winnerSaved = true;
            }

            List<Ranking> rankings =
                    rankingRows.stream()
                            .map(row -> Ranking.builder()
                                    .season(season)
                                    .user(getUserReference(row.getUserId()))
                                    .nickname(row.getNickname())
                                    .score(row.getScore())
                                    .rank(row.getRank())
                                    .snapshottedAt(snapshottedAt)
                                    .build())
                            .toList();

            rankingRepository.saveAll(rankings);
            offset += SNAPSHOT_BATCH_SIZE;
        }
    }

    private void saveWinner(
            Season season,
            List<RankingRow> rankingRows,
            OffsetDateTime decidedAt
    ) {

        if (rankingRows.isEmpty()
                || winnerRepository.existsBySeasonId(season.getId())) {
            return;
        }

        RankingRow winner = rankingRows.get(0);

        winnerRepository.save(
                SeasonWinner.builder()
                        .season(season)
                        .user(getUserReference(winner.getUserId()))
                        .nickname(winner.getNickname())
                        .score(winner.getScore())
                        .decidedAt(decidedAt)
                        .build()
        );
    }

    private User getUserReference(Long userId) {

        return userRepository.getReferenceById(userId);
    }
}
