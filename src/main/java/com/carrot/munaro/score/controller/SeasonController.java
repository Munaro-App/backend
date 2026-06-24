package com.carrot.munaro.score.controller;

import com.carrot.munaro.global.response.ApiResponse;
import com.carrot.munaro.score.dto.response.SeasonResponse;
import com.carrot.munaro.score.service.SeasonService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seasons")
@RequiredArgsConstructor
public class SeasonController {

    private final SeasonService seasonService;

    @GetMapping("/current")
    public ApiResponse<SeasonResponse> getCurrentSeason() {

        return ApiResponse.ok(seasonService.getCurrentSeason());
    }
}
