package com.carrot.munaro.score.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SeasonRolloverScheduler {

    @Scheduled(cron = "0 0 0 1 * *")
    public void rolloverSeason() {

    }
}