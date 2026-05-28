package com.ticketsystem.repository;

import com.ticketsystem.entity.SlaConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SlaConfigRepository extends JpaRepository<SlaConfig, Long> {

    List<SlaConfig> findByProjectId(Long projectId);

    Optional<SlaConfig> findByProjectIdAndPriorityLevel(Long projectId, String priorityLevel);

    boolean existsByProjectIdAndPriorityLevel(Long projectId, String priorityLevel);
}
