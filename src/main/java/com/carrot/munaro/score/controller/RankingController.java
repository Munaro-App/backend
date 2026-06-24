package com.carrot.munaro.score.controller;

import com.carrot.munaro.global.response.ApiResponse;
import com.carrot.munaro.score.dto.response.MyRankingResponse;
import com.carrot.munaro.score.dto.response.RankingResponse;
import com.carrot.munaro.score.service.RankingQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingQueryService rankingQueryService;

    @GetMapping("/current")
    public ApiResponse<RankingResponse> getCurrentSeasonRanking() {

        return ApiResponse.ok(
                rankingQueryService.getCurrentSeasonRanking()
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

    @GetMapping("/current/top3")
    public ApiResponse<RankingResponse> getCurrentSeasonTop3Ranking() {

        return ApiResponse.ok(
                rankingQueryService.getCurrentSeasonTop3Ranking()
        );
    }

    @GetMapping("/seasons/{seasonId}")
    public ApiResponse<RankingResponse> getSeasonRanking(
            @PathVariable Long seasonId
    ) {

        return ApiResponse.ok(
                rankingQueryService.getSeasonRanking(seasonId)
        );
    }

    @GetMapping("/seasons/{seasonId}/me")
    public ApiResponse<MyRankingResponse> getMySeasonRanking(
            @PathVariable Long seasonId,
            Authentication authentication
    ) {

        Long userId = (Long) authentication.getPrincipal();

        return ApiResponse.ok(
                rankingQueryService.getMySeasonRanking(seasonId, userId)
        );
    }

    @GetMapping("/seasons/{seasonId}/top3")
    public ApiResponse<RankingResponse> getSeasonTop3Ranking(
            @PathVariable Long seasonId
    ) {

        return ApiResponse.ok(
                rankingQueryService.getSeasonTop3Ranking(seasonId)
        );
    }
}
