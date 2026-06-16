package com.carrot.munaro.score.service;

import com.carrot.munaro.score.domain.Score;
import com.carrot.munaro.score.domain.ScoreSource;
import com.carrot.munaro.score.domain.Season;
import com.carrot.munaro.score.repository.ScoreRepository;
import com.carrot.munaro.user.domain.User;
import com.carrot.munaro.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScoreService {

    private final ScoreRepository scoreRepository;
    private final SeasonService seasonService;
    private final UserRepository userRepository;

    @Transactional
    public void addScore(
            Long userId,
            int score,
            ScoreSource source
    ) {

        if (score <= 0) {
            return;
        }

        Season season = seasonService.getOrCreateCurrentSeason();
        User user = userRepository.getReferenceById(userId);

        scoreRepository.save(
                Score.builder()
                        .user(user)
                        .season(season)
                        .score(score)
                        .source(source)
                        .build()
        );
    }
}
