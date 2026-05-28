package com.ticketsystem.repository;

import com.ticketsystem.entity.SlaConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SlaConfigRepository extends JpaRepository<SlaConfig, Long> {
    Optional<SlaConfig> findByPriorityAndSupportLevel(String priority, String supportLevel);
    List<SlaConfig> findByIsActiveTrue();
}
