package com.carrot.munaro.mission.repository;

import com.carrot.munaro.mission.domain.PhotoMission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoMissionRepository
        extends JpaRepository<PhotoMission, Long> {
}