package com.ticketsystem.repository;

import com.ticketsystem.entity.Ticket;
import com.ticketsystem.entity.Ticket.CurrentStatus;
import com.ticketsystem.entity.Ticket.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketId(String ticketId);

    boolean existsByTicketId(String ticketId);

    long countByCurrentStatus(CurrentStatus status);

    // Search with optional filters
    @Query("SELECT t FROM Ticket t WHERE " +
           "(:ticketId IS NULL OR LOWER(t.ticketId) LIKE LOWER(CONCAT('%', :ticketId, '%'))) AND " +
           "(:projectAssignment IS NULL OR LOWER(t.projectAssignment) LIKE LOWER(CONCAT('%', :projectAssignment, '%'))) AND " +
           "(:status IS NULL OR t.currentStatus = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority)")
    Page<Ticket> searchTickets(
            @Param("ticketId") String ticketId,
            @Param("projectAssignment") String projectAssignment,
            @Param("status") CurrentStatus status,
            @Param("priority") Priority priority,
            Pageable pageable
    );

    // Find tickets assigned to a specific employee (for My Tickets view)
    @Query("SELECT t FROM Ticket t WHERE " +
           "LOWER(t.assignedEmployee) LIKE LOWER(CONCAT('%', :assignedEmployee, '%')) AND " +
           "(:status IS NULL OR t.currentStatus = :status)")
    Page<Ticket> findByAssignedEmployee(
            @Param("assignedEmployee") String assignedEmployee,
            @Param("status") CurrentStatus status,
            Pageable pageable
    );

    // Find tickets for employee via their employee name (mapped from User → Employee)
    @Query("SELECT t FROM Ticket t WHERE " +
           "LOWER(t.assignedEmployee) LIKE LOWER(CONCAT('%', :employeeName, '%')) AND " +
           "(:status IS NULL OR t.currentStatus = :status)")
    Page<Ticket> findByEmployeeName(
            @Param("employeeName") String employeeName,
            @Param("status") CurrentStatus status,
            Pageable pageable
    );

    // Find the highest ticket sequence number for auto-generation
    @Query("SELECT MAX(CAST(SUBSTRING(t.ticketId, 5) AS int)) FROM Ticket t WHERE t.ticketId LIKE 'INC-%'")
    Integer findMaxTicketSequence();
}
