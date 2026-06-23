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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeasonService {

    private static final ZoneId SEASON_ZONE = ZoneId.of("Asia/Seoul");

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

        SeasonPeriod currentSeasonPeriod = getSeasonPeriod(
                OffsetDateTime.now(SEASON_ZONE)
        );

        return seasonRepository
                .findFirstByStartedAtAndEndedAt(
                        currentSeasonPeriod.startedAt(),
                        currentSeasonPeriod.endedAt()
                )
                .orElseGet(() -> createSeason(currentSeasonPeriod));
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

    private Season createSeason(SeasonPeriod seasonPeriod) {

        return seasonRepository.save(
                Season.builder()
                        .seasonName(seasonPeriod.seasonName())
                        .startedAt(seasonPeriod.startedAt())
                        .endedAt(seasonPeriod.endedAt())
                        .createdAt(OffsetDateTime.now(SEASON_ZONE))
                        .build()
        );
    }

    private SeasonPeriod getSeasonPeriod(OffsetDateTime now) {

        int year = now.getYear();
        int month = now.getMonthValue();

        return switch (month) {
            case 3, 4, 5 -> createSeasonPeriod(
                    "봄 시즌",
                    year,
                    3,
                    year,
                    6
            );
            case 6, 7, 8 -> createSeasonPeriod(
                    "여름 시즌",
                    year,
                    6,
                    year,
                    9
            );
            case 9, 10, 11 -> createSeasonPeriod(
                    "가을 시즌",
                    year,
                    9,
                    year,
                    12
            );
            case 12 -> createSeasonPeriod(
                    "겨울 시즌",
                    year,
                    12,
                    year + 1,
                    3
            );
            case 1, 2 -> createSeasonPeriod(
                    "겨울 시즌",
                    year - 1,
                    12,
                    year,
                    3
            );
            default -> throw new IllegalStateException(
                    "Invalid month: " + month
            );
        };
    }

    private SeasonPeriod createSeasonPeriod(
            String seasonName,
            int startYear,
            int startMonth,
            int endYear,
            int endMonth
    ) {

        return new SeasonPeriod(
                seasonName,
                LocalDate.of(startYear, startMonth, 1)
                        .atStartOfDay(SEASON_ZONE)
                        .toOffsetDateTime(),
                LocalDate.of(endYear, endMonth, 1)
                        .atStartOfDay(SEASON_ZONE)
                        .toOffsetDateTime()
        );
    }

    private record SeasonPeriod(
            String seasonName,
            OffsetDateTime startedAt,
            OffsetDateTime endedAt
    ) {
    }
}
