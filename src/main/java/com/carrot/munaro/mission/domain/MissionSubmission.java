package com.carrot.munaro.mission.domain;

import com.carrot.munaro.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "mission_submissions")
public class MissionSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mission_submission_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_mission_id", nullable = false)
    private PhotoMission photoMission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionType submissionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus submissionStatus;
}
