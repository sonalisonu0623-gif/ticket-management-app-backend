package com.ticketsystem.repository;

import com.ticketsystem.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findBySupportLevel(String supportLevel);
    List<Employee> findByStatus(String status);
    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByEmployeeId(String employeeId);

    /** All employees assigned to a project */
    @Query("SELECT e FROM Employee e JOIN e.projects p WHERE p.id = :projectId")
    List<Employee> findByProjectId(Long projectId);

    /** Employees assigned to a project with a specific support level */
    @Query("SELECT e FROM Employee e JOIN e.projects p WHERE p.id = :projectId AND e.supportLevel = :level")
    List<Employee> findByProjectIdAndSupportLevel(Long projectId, String level);

    boolean existsByEmail(String email);
    boolean existsByEmployeeId(String employeeId);
}