package com.carrot.munaro.mission.repository;

import com.carrot.munaro.mission.domain.MissionSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionSubmissionRepository
        extends JpaRepository<MissionSubmission, Long> {
}