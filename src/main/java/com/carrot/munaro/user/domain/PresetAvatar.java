package com.carrot.munaro.user.domain;

import java.util.Arrays;

public enum PresetAvatar {
    LION,
    TIGER,
    WOLF,
    FOX,
    BEAR,
    RACCOON,
    EAGLE,
    DOLPHIN;

    public static boolean contains(String value) {

        if (value == null || value.isBlank()) {
            return false;
        }

        return Arrays.stream(values())
                .anyMatch(preset -> preset.name().equals(value.trim()));
    }
}
