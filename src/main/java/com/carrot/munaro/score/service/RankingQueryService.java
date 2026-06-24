package com.carrot.munaro.score.service;

import com.carrot.munaro.score.domain.Season;
import com.carrot.munaro.score.dto.response.MyRankingResponse;
import com.carrot.munaro.score.dto.response.RankingItemResponse;
import com.carrot.munaro.score.dto.response.RankingResponse;
import com.carrot.munaro.score.repository.RankingRepository;
import com.carrot.munaro.score.repository.ScoreRepository;
import com.carrot.munaro.score.repository.projection.RankingRow;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingQueryService {

    private final SeasonService seasonService;
    private final ScoreRepository scoreRepository;
    private final RankingRepository rankingRepository;

    @Transactional(readOnly = true)
    public RankingResponse getCurrentSeasonRanking() {

        Season season = seasonService.getOrCreateCurrentSeason();
        return getRanking(season);
    }

    @Transactional(readOnly = true)
    public RankingResponse getSeasonRanking(Long seasonId) {

        Season season = seasonService.getSeason(seasonId);
        return getRanking(season);
    }

    @Transactional(readOnly = true)
    public MyRankingResponse getMyCurrentSeasonRanking(Long userId) {

        Season season = seasonService.getOrCreateCurrentSeason();
        return getMyRanking(season, userId);
    }

    @Transactional(readOnly = true)
    public MyRankingResponse getMySeasonRanking(
            Long seasonId,
            Long userId
    ) {

        Season season = seasonService.getSeason(seasonId);
        return getMyRanking(season, userId);
    }

    @Transactional(readOnly = true)
    public RankingResponse getCurrentSeasonTop3Ranking() {

        Season season = seasonService.getOrCreateCurrentSeason();
        return getTop3Ranking(season);
    }

    @Transactional(readOnly = true)
    public RankingResponse getSeasonTop3Ranking(Long seasonId) {

        Season season = seasonService.getSeason(seasonId);
        return getTop3Ranking(season);
    }

    private MyRankingResponse getMyRanking(
            Season season,
            Long userId
    ) {

        RankingRow row = scoreRepository.findMyRankingRow(
                season.getId(),
                userId
        );

        if (row == null) {
            return new MyRankingResponse(null, 0);
        }

        return new MyRankingResponse(row.getRank(), row.getScore());
    }

    private RankingResponse getRanking(Season season) {

        List<RankingRow> rows;

        if (season.isActive(java.time.OffsetDateTime.now())
                || !rankingRepository.existsBySeasonId(season.getId())) {
            rows = scoreRepository.findRankingRows(season.getId());
        } else {
            rows = rankingRepository.findRankingRowsBySeasonId(season.getId());
        }

        return new RankingResponse(
                season.getId(),
                season.getSeasonName(),
                rows.stream()
                        .map(this::toRankingItem)
                        .toList()
        );
    }

    private RankingResponse getTop3Ranking(Season season) {

        List<RankingRow> rows;

        if (season.isActive(java.time.OffsetDateTime.now())
                || !rankingRepository.existsBySeasonId(season.getId())) {
            rows = scoreRepository.findRankingRows(season.getId(), 3, 0);
        } else {
            rows = rankingRepository.findRankingRowsBySeasonId(
                    season.getId(),
                    PageRequest.of(0, 3)
            );
        }

        return new RankingResponse(
                season.getId(),
                season.getSeasonName(),
                rows.stream()
                        .map(this::toRankingItem)
                        .toList()
        );
    }

    private RankingItemResponse toRankingItem(RankingRow row) {

        return new RankingItemResponse(
                row.getUserId(),
                row.getNickname(),
                row.getScore(),
                row.getRank()
        );
    }
}
