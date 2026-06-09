package com.carrot.munaro.tourist_spot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tourist-spots")
@RequiredArgsConstructor
public class TouristSpotController {

    @GetMapping("/search")
    public String search(
            @RequestParam String keyword
    ) {
        return keyword;
    }
}