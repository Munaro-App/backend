package com.carrot.munaro.score.service;

import com.carrot.munaro.score.domain.Season;
import com.carrot.munaro.score.dto.response.MyRankingResponse;
import com.carrot.munaro.score.dto.response.RankingItemResponse;
import com.carrot.munaro.score.dto.response.RankingPageResponse;
import com.carrot.munaro.score.repository.RankingRepository;
import com.carrot.munaro.score.repository.ScoreRepository;
import com.carrot.munaro.score.repository.projection.RankingRow;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public RankingPageResponse getCurrentSeasonRanking(
            int page,
            int size
    ) {

        Season season = seasonService.getOrCreateCurrentSeason();
        return getRanking(season, page, size);
    }

    @Transactional(readOnly = true)
    public RankingPageResponse getSeasonRanking(
            Long seasonId,
            int page,
            int size
    ) {

        Season season = seasonService.getSeason(seasonId);
        return getRanking(season, page, size);
    }

    @Transactional(readOnly = true)
    public MyRankingResponse getMyCurrentSeasonRanking(Long userId) {

        Season season = seasonService.getOrCreateCurrentSeason();
        RankingRow row = scoreRepository.findMyRankingRow(
                season.getId(),
                userId
        );

        if (row == null) {
            return new MyRankingResponse(null, 0);
        }

        return new MyRankingResponse(row.getRank(), row.getScore());
    }

    private RankingPageResponse getRanking(
            Season season,
            int page,
            int size
    ) {

        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int offset = normalizedPage * normalizedSize;

        List<RankingRow> rows;
        long totalElements;

        if (season.isActive(java.time.OffsetDateTime.now())
                || !rankingRepository.existsBySeasonId(season.getId())) {
            rows = scoreRepository.findRankingRows(
                    season.getId(),
                    normalizedSize,
                    offset
            );
            totalElements = scoreRepository.countRankingUsers(season.getId());
        } else {
            Pageable pageable =
                    PageRequest.of(normalizedPage, normalizedSize);
            rows = rankingRepository.findRankingRowsBySeasonId(
                    season.getId(),
                    pageable
            );
            totalElements = rankingRepository.countBySeasonId(season.getId());
        }

        return new RankingPageResponse(
                season.getId(),
                season.getSeasonName(),
                rows.stream()
                        .map(this::toRankingItem)
                        .toList(),
                normalizedPage,
                normalizedSize,
                totalElements,
                calculateTotalPages(totalElements, normalizedSize)
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

    private int calculateTotalPages(
            long totalElements,
            int size
    ) {

        if (totalElements == 0) {
            return 0;
        }

        return (int) Math.ceil((double) totalElements / size);
    }
}
