package com.ticketsystem.repository;

import com.ticketsystem.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    Page<Ticket> findByCreatedBy(User createdBy, Pageable pageable);

    Page<Ticket> findByAssignedTo(User assignedTo, Pageable pageable);

    List<Ticket> findByCurrentStatus(TicketStatus status);

    List<Ticket> findByPriority(Priority priority);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.currentStatus = :status")
    Long countByStatus(@Param("status") TicketStatus status);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.priority = :priority")
    Long countByPriority(@Param("priority") Priority priority);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.slaBreached = true")
    Long countSlaBreached();

    @Query("SELECT t FROM Ticket t WHERE t.currentStatus NOT IN ('RESOLVED', 'CLOSED') " +
           "AND t.slaDueDateTime < :now AND t.slaBreached = false")
    List<Ticket> findTicketsExceedingSla(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.createdAt >= :startDate AND t.createdAt < :endDate")
    Long countByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t.currentStatus, COUNT(t) FROM Ticket t GROUP BY t.currentStatus")
    List<Object[]> getTicketStatusDistribution();

    @Query("SELECT t.priority, COUNT(t) FROM Ticket t GROUP BY t.priority")
    List<Object[]> getTicketPriorityDistribution();

    @Query("SELECT t.assignedTo.fullName, COUNT(t) FROM Ticket t WHERE t.assignedTo IS NOT NULL GROUP BY t.assignedTo")
    List<Object[]> getEmployeeWorkload();

    @Query("SELECT MONTH(t.createdAt), YEAR(t.createdAt), COUNT(t) FROM Ticket t " +
           "WHERE t.createdAt >= :startDate GROUP BY MONTH(t.createdAt), YEAR(t.createdAt) ORDER BY YEAR(t.createdAt), MONTH(t.createdAt)")
    List<Object[]> getMonthlyTrends(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT AVG(t.resolutionTimeMinutes) FROM Ticket t WHERE t.resolutionTimeMinutes IS NOT NULL")
    Double getAverageResolutionTime();

    @Query("SELECT t FROM Ticket t WHERE t.ticketNumber LIKE %:query% OR t.issueDescription LIKE %:query%")
    Page<Ticket> searchTickets(@Param("query") String query, Pageable pageable);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(t.ticketNumber, 5) AS int)), 9000) FROM Ticket t")
    Integer getMaxTicketSequence();
}
