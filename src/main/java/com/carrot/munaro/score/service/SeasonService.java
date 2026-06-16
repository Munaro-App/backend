package com.carrot.munaro.score.service;

import com.carrot.munaro.global.exception.BusinessException;
import com.carrot.munaro.global.exception.ErrorCode;
import com.carrot.munaro.score.domain.Season;
import com.carrot.munaro.score.dto.response.SeasonRolloverResponse;
import com.carrot.munaro.score.dto.response.SeasonResponse;
import com.carrot.munaro.score.repository.SeasonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeasonService {

    private static final ZoneId SEASON_ZONE = ZoneId.of("Asia/Seoul");
    private static final YearMonth FIRST_SEASON_MONTH = YearMonth.of(2026, 6);

    private final SeasonRepository seasonRepository;
    private final RankingSnapshotService rankingSnapshotService;

    @Transactional(readOnly = true)
    public Season getSeason(Long seasonId) {

        return seasonRepository.findById(seasonId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.SEASON_NOT_FOUND)
                );
    }

    @Transactional
    public Season getOrCreateCurrentSeason() {

        OffsetDateTime now = OffsetDateTime.now(SEASON_ZONE);

        return seasonRepository
                .findFirstByStartedAtLessThanEqualAndEndedAtGreaterThan(
                        now,
                        now
                )
                .orElseGet(this::createCurrentMonthSeason);
    }

    @Transactional
    public SeasonResponse getCurrentSeason() {

        Season season = getOrCreateCurrentSeason();
        return SeasonResponse.from(season);
    }

    @Transactional
    public SeasonRolloverResponse rolloverExpiredSeasons() {

        OffsetDateTime now = OffsetDateTime.now(SEASON_ZONE);
        List<Season> expiredSeasons =
                seasonRepository.findByEndedAtLessThanEqual(now);

        int savedRankingCount = 0;
        int skippedSeasonCount = 0;

        for (Season season : expiredSeasons) {
            int snapshotCount = rankingSnapshotService.snapshotSeason(
                    season,
                    now
            );

            if (snapshotCount < 0) {
                skippedSeasonCount++;
            } else {
                savedRankingCount += snapshotCount;
            }

            season.close();
        }

        getOrCreateCurrentSeason();

        return new SeasonRolloverResponse(
                expiredSeasons.size(),
                skippedSeasonCount,
                savedRankingCount
        );
    }

    private Season createCurrentMonthSeason() {

        YearMonth currentMonth = YearMonth.now(SEASON_ZONE);
        String seasonName = createSeasonName(currentMonth);

        return seasonRepository.save(
                Season.builder()
                        .seasonName(seasonName)
                        .startedAt(
                                currentMonth.atDay(1)
                                        .atStartOfDay(SEASON_ZONE)
                                        .toOffsetDateTime()
                        )
                        .endedAt(
                                currentMonth.plusMonths(1)
                                        .atDay(1)
                                        .atStartOfDay(SEASON_ZONE)
                                        .toOffsetDateTime()
                        )
                        .createdAt(OffsetDateTime.now(SEASON_ZONE))
                        .build()
        );
    }

    private String createSeasonName(YearMonth seasonMonth) {

        long seasonNumber =
                ChronoUnit.MONTHS.between(
                        FIRST_SEASON_MONTH,
                        seasonMonth
                ) + 1;

        if (seasonNumber < 1) {
            seasonNumber = 1;
        }

        return "시즌 " + seasonNumber;
    }
}
