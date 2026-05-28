package com.ticketsystem.repository;

import com.ticketsystem.entity.Employee;
import com.ticketsystem.entity.Employee.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByUsername(String username);
    Optional<Employee> findByEmail(String email);

    /** Look up by business name — used by SLA service to resolve ticket assignee → shift */
    Optional<Employee> findByEmployeeName(String employeeName);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByUsernameAndIdNot(String username, Long id);

    @Query("""
        SELECT e FROM Employee e
        WHERE (:search IS NULL OR :search = ''
               OR LOWER(e.employeeName) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(e.email)        LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(e.username)     LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:status IS NULL OR e.status = :status)
        """)
    Page<Employee> searchEmployees(
            @Param("search") String search,
            @Param("status") EmployeeStatus status,
            Pageable pageable);
}
