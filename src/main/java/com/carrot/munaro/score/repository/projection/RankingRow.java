package com.carrot.munaro.score.repository.projection;

public interface RankingRow {

    Long getUserId();

    String getNickname();

    Integer getScore();

    Integer getRank();
}
