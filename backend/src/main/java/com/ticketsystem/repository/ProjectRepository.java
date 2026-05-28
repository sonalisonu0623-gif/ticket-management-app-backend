package com.ticketsystem.repository;

import com.ticketsystem.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    boolean existsByProjectName(String projectName);
    boolean existsByProjectCode(String projectCode);
    Optional<Project> findByProjectCode(String projectCode);

    List<Project> findByStatus(String status);

    /** Projects that contain a given employee */
    @Query("SELECT p FROM Project p JOIN p.employees e WHERE e.id = :employeeId")
    List<Project> findByEmployeeId(Long employeeId);

    /** Active projects ordered by name */
    List<Project> findByStatusOrderByProjectNameAsc(String status);
}
