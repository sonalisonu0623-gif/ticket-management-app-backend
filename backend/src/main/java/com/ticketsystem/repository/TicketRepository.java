package com.ticketsystem.repository;

import com.ticketsystem.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>,
                                          JpaSpecificationExecutor<Ticket> {

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    Page<Ticket> findByProjectId(Long projectId, Pageable pageable);

    List<Ticket> findByProjectId(Long projectId);

    List<Ticket> findByAssignedEmployeeId(Long employeeId);

    List<Ticket> findByProjectIdAndCurrentStatus(Long projectId, String status);

    List<Ticket> findByCurrentStatus(String status);

    long countByProjectId(Long projectId);

    long countByProjectIdAndCurrentStatus(Long projectId, String status);

    long countByProjectIdAndSlaBreached(Long projectId, Boolean slaBreached);

    long countByProjectIdAndPriority(Long projectId, String priority);

    /** For thread-safe ticket numbering */
    @Query("SELECT MAX(CAST(SUBSTRING(t.ticketNumber, 5) AS int)) " +
           "FROM Ticket t WHERE t.ticketNumber LIKE 'INC-%'")
    Integer findMaxTicketSequence();

    /** Tickets created within a date range for a project */
    @Query("SELECT t FROM Ticket t WHERE t.project.id = :projectId " +
           "AND t.createdAt BETWEEN :from AND :to ORDER BY t.createdAt DESC")
    List<Ticket> findByProjectAndDateRange(Long projectId,
                                           LocalDateTime from,
                                           LocalDateTime to);

    /** All open/in-progress tickets to check SLA breaches */
    @Query("SELECT t FROM Ticket t WHERE t.currentStatus NOT IN ('Resolved','Closed') " +
           "AND t.generationDatetime IS NOT NULL")
    List<Ticket> findActiveTickets();

    /** Count tickets by status for a project */
    @Query("SELECT t.currentStatus, COUNT(t) FROM Ticket t WHERE t.project.id = :projectId " +
           "GROUP BY t.currentStatus")
    List<Object[]> countByStatusForProject(Long projectId);

    /** Count tickets by priority for a project */
    @Query("SELECT t.priority, COUNT(t) FROM Ticket t WHERE t.project.id = :projectId " +
           "GROUP BY t.priority")
    List<Object[]> countByPriorityForProject(Long projectId);

    /** Employee performance stats per project */
    @Query("SELECT t.assignedEmployee.id, t.assignedEmployee.employeeName, " +
           "COUNT(t), " +
           "SUM(CASE WHEN t.currentStatus IN ('Resolved','Closed') THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN t.slaBreached = true THEN 1 ELSE 0 END) " +
           "FROM Ticket t WHERE t.project.id = :projectId " +
           "AND t.assignedEmployee IS NOT NULL " +
           "GROUP BY t.assignedEmployee.id, t.assignedEmployee.employeeName")
    List<Object[]> getEmployeePerformanceByProject(Long projectId);
}
