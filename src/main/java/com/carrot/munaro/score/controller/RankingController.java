package com.carrot.munaro.score.controller;

import com.carrot.munaro.global.response.ApiResponse;
import com.carrot.munaro.score.dto.response.MyRankingResponse;
import com.carrot.munaro.score.dto.response.RankingPageResponse;
import com.carrot.munaro.score.service.RankingQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingQueryService rankingQueryService;

    @GetMapping("/current")
    public ApiResponse<RankingPageResponse> getCurrentSeasonRanking(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        return ApiResponse.ok(
                rankingQueryService.getCurrentSeasonRanking(page, size)
        );
    }

    @GetMapping("/current/me")
    public ApiResponse<MyRankingResponse> getMyCurrentSeasonRanking(
            Authentication authentication
    ) {

        Long userId = (Long) authentication.getPrincipal();

        return ApiResponse.ok(
                rankingQueryService.getMyCurrentSeasonRanking(userId)
        );
    }

    @GetMapping("/seasons/{seasonId}")
    public ApiResponse<RankingPageResponse> getSeasonRanking(
            @PathVariable Long seasonId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        return ApiResponse.ok(
                rankingQueryService.getSeasonRanking(
                        seasonId,
                        page,
                        size
                )
        );
    }
}
