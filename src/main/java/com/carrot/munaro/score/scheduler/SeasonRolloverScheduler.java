package com.carrot.munaro.score.scheduler;

import com.carrot.munaro.score.service.SeasonService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SeasonRolloverScheduler {

    private final SeasonService seasonService;

    @Scheduled(cron = "0 5 * * * *", zone = "Asia/Seoul")
    public void rolloverSeason() {

        seasonService.rolloverExpiredSeasons();
    }
}
