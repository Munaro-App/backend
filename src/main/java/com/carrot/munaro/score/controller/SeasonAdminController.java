package com.carrot.munaro.score.controller;

import com.carrot.munaro.global.response.ApiResponse;
import com.carrot.munaro.score.dto.response.SeasonRolloverResponse;
import com.carrot.munaro.score.service.SeasonService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/seasons")
@RequiredArgsConstructor
public class SeasonAdminController {

    private final SeasonService seasonService;

    @PostMapping("/rollover")
    public ApiResponse<SeasonRolloverResponse> rolloverExpiredSeasons() {

        return ApiResponse.ok(seasonService.rolloverExpiredSeasons());
    }
}
