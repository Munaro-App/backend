package com.carrot.munaro.user.service;

import com.carrot.munaro.global.exception.BusinessException;
import com.carrot.munaro.global.exception.ErrorCode;
import com.carrot.munaro.quiz.repository.QuizSubmissionRepository;
import com.carrot.munaro.score.domain.Season;
import com.carrot.munaro.score.repository.ScoreRepository;
import com.carrot.munaro.score.repository.SeasonRepository;
import com.carrot.munaro.score.repository.projection.RankingRow;
import com.carrot.munaro.user.domain.Badge;
import com.carrot.munaro.user.domain.Profile;
import com.carrot.munaro.user.domain.User;
import com.carrot.munaro.user.domain.UserBadge;
import com.carrot.munaro.user.domain.UserVisitedTouristSpot;
import com.carrot.munaro.user.dto.response.BadgeResponse;
import com.carrot.munaro.user.dto.response.UserResponse;
import com.carrot.munaro.user.dto.response.UserStatisticsResponse;
import com.carrot.munaro.user.repository.UserBadgeRepository;
import com.carrot.munaro.user.repository.UserRepository;
import com.carrot.munaro.user.repository.UserVisitedTouristSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserVisitedTouristSpotRepository
            userVisitedTouristSpotRepository;
    private final ScoreRepository scoreRepository;
    private final SeasonRepository seasonRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final UserBadgeRepository userBadgeRepository;

    @Transactional(readOnly = true)
    public UserResponse getMe(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.USER_NOT_FOUND)
                );

        Profile profile = user.getProfile();
        List<UserVisitedTouristSpot> visitedTouristSpots =
                userVisitedTouristSpotRepository
                        .findByUser_IdOrderByVisitedAtDesc(userId);
        List<String> visitedSidos = visitedTouristSpots.stream()
                .map(visited -> visited.getTouristSpot().getSido())
                .filter(sido -> sido != null && !sido.isBlank())
                .distinct()
                .toList();
        UserStatisticsResponse statistics =
                buildStatistics(userId, visitedSidos);
        List<BadgeResponse> recentBadges =
                userBadgeRepository.findTop3ByUser_IdOrderByEarnedAtDesc(
                                userId
                        )
                        .stream()
                        .map(this::toBadgeResponse)
                        .toList();

        return UserResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .avatarValue(
                        profile != null
                                ? profile.getAvatarValue()
                                : null
                )
                .seasonRank(getCurrentSeasonRank(userId))
                .statistics(statistics)
                .visitedSidos(visitedSidos)
                .recentBadges(recentBadges)
                .build();
    }

    private UserStatisticsResponse buildStatistics(
            Long userId,
            List<String> visitedSidos
    ) {

        return UserStatisticsResponse.builder()
                .totalPoints(scoreRepository.sumPointsByUserId(userId))
                .completedSpots((int) userVisitedTouristSpotRepository
                        .countByUser_Id(userId))
                .visitedSidoCount(visitedSidos.size())
                .perfectCount((int) quizSubmissionRepository
                        .countPerfectSubmissionsByUserId(userId))
                .build();
    }

    private Integer getCurrentSeasonRank(Long userId) {

        Optional<Season> currentSeason =
                seasonRepository
                        .findFirstByStartedAtLessThanEqualAndEndedAtGreaterThan(
                                OffsetDateTime.now(),
                                OffsetDateTime.now()
                        );

        if (currentSeason.isEmpty()) {
            return null;
        }

        RankingRow rankingRow = scoreRepository.findMyRankingRow(
                currentSeason.get().getId(),
                userId
        );

        if (rankingRow == null) {
            return null;
        }

        return rankingRow.getRank();
    }

    private BadgeResponse toBadgeResponse(UserBadge userBadge) {

        Badge badge = userBadge.getBadge();

        return BadgeResponse.builder()
                .badgeId(badge.getId())
                .badgeName(badge.getBadgeName())
                .badgeType(badge.getBadgeType())
                .badgeImageUrl(badge.getBadgeImageUrl())
                .build();
    }

}
