package com.carrot.munaro.score.service;

import com.carrot.munaro.global.exception.BusinessException;
import com.carrot.munaro.global.exception.ErrorCode;
import com.carrot.munaro.score.domain.Season;
import com.carrot.munaro.score.dto.response.SeasonResponse;
import com.carrot.munaro.score.repository.SeasonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.YearMonth;
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

        return seasonRepository.findByActiveTrue()
                .orElseGet(this::createCurrentMonthSeason);
    }

    @Transactional
    public SeasonResponse getCurrentSeason() {

        Season season = getOrCreateCurrentSeason();
        return SeasonResponse.from(season);
    }

    @Transactional
    public void rolloverExpiredSeasons() {

        OffsetDateTime now = OffsetDateTime.now(SEASON_ZONE);
        List<Season> expiredSeasons =
                seasonRepository.findByActiveTrueAndEndedAtLessThanEqual(now);

        for (Season season : expiredSeasons) {
            rankingSnapshotService.snapshotSeason(season, now);
            season.close(now);
        }

        seasonRepository.saveAll(expiredSeasons);
        getOrCreateCurrentSeason();
    }

    private Season createCurrentMonthSeason() {

        YearMonth currentMonth = YearMonth.now(SEASON_ZONE);
        String seasonName = currentMonth + " 시즌";

        return seasonRepository.findBySeasonName(seasonName)
                .orElseGet(() -> seasonRepository.save(
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
                                .active(true)
                                .build()
                ));
    }
}
