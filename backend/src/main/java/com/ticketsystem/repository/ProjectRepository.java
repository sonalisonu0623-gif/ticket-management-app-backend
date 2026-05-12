package com.ticketsystem.repository;

import com.ticketsystem.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByName(String name);
    Optional<Project> findByProjectCode(String projectCode);
    List<Project> findByIsActive(Boolean isActive);
    boolean existsByName(String name);
    boolean existsByProjectCode(String projectCode);
}
