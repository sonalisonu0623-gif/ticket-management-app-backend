package com.ticketsystem.service;

import com.ticketsystem.dto.*;
import com.ticketsystem.entity.Project;
import com.ticketsystem.entity.Ticket;
import com.ticketsystem.exception.ResourceNotFoundException;
import com.ticketsystem.repository.ProjectRepository;
import com.ticketsystem.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final TicketRepository    ticketRepository;
    private final ProjectRepository   projectRepository;
    private final TicketService       ticketService;
    private final BusinessHoursCalculator bh;

    /**
     * Returns a full project-specific dashboard.
     * If projectId is null, returns aggregate across all projects.
     */
    public DashboardDTO getDashboard(Long projectId) {
        List<Ticket> tickets;
        String projectName = "All Projects";

        if (projectId != null) {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
            tickets     = ticketRepository.findByProjectId(projectId);
            projectName = project.getProjectName();
        } else {
            tickets = ticketRepository.findAll();
        }

        long total     = tickets.size();
        long open      = count(tickets, "Open");
        long inProg    = count(tickets, "In Progress");
        long pending   = count(tickets, "Pending");
        long resolved  = countMultiple(tickets, "Resolved", "Closed");
        long closed    = count(tickets, "Closed");
        long escalated = count(tickets, "Escalated");
        long critical  = tickets.stream().filter(t -> "P1 - Critical".equals(t.getPriority())).count();
        long breached  = tickets.stream().filter(t -> Boolean.TRUE.equals(t.getSlaBreached())).count();

        // Average resolution hours (for resolved/closed tickets with elapsed time)
        OptionalDouble avgRes = tickets.stream()
                .filter(t -> ("Resolved".equals(t.getCurrentStatus()) || "Closed".equals(t.getCurrentStatus()))
                          && t.getBusinessHoursElapsed() != null && t.getBusinessHoursElapsed() > 0)
                .mapToInt(Ticket::getBusinessHoursElapsed)
                .average();

        double slaCompliance = total > 0
                ? Math.round(((total - breached) * 100.0 / total) * 10.0) / 10.0
                : 100.0;

        // Grouped counts
        Map<String, Long> byStatus   = groupBy(tickets, t -> t.getCurrentStatus());
        Map<String, Long> byPriority = groupBy(tickets, t -> t.getPriority());
        Map<String, Long> byLevel    = groupBy(tickets, t -> t.getSupportLevel());

        // Employee performance
        List<EmployeePerformanceDTO> performance = buildPerformance(tickets);

        // Recent 10 tickets
        List<TicketDTO> recent = tickets.stream()
                .sorted(Comparator.comparing(
                        t -> t.getCreatedAt() != null ? t.getCreatedAt() : LocalDateTime.MIN,
                        Comparator.reverseOrder()))
                .limit(10)
                .map(ticketService::toDTO)
                .collect(Collectors.toList());

        // Open critical tickets
        List<TicketDTO> criticalOpen = tickets.stream()
                .filter(t -> "P1 - Critical".equals(t.getPriority())
                          && !"Resolved".equals(t.getCurrentStatus())
                          && !"Closed".equals(t.getCurrentStatus()))
                .map(ticketService::toDTO)
                .collect(Collectors.toList());

        return DashboardDTO.builder()
                .projectId(projectId)
                .projectName(projectName)
                .totalTickets(total)
                .openTickets(open)
                .inProgressTickets(inProg)
                .pendingTickets(pending)
                .resolvedTickets(resolved)
                .closedTickets(closed)
                .escalatedTickets(escalated)
                .criticalTickets(critical)
                .slaBreachedTickets(breached)
                .avgResolutionHours(avgRes.isPresent() ? Math.round(avgRes.getAsDouble() * 10.0) / 10.0 : null)
                .slaComplianceRate(slaCompliance)
                .ticketsByStatus(byStatus)
                .ticketsByPriority(byPriority)
                .ticketsBySupportLevel(byLevel)
                .employeePerformance(performance)
                .recentTickets(recent)
                .criticalOpenTickets(criticalOpen)
                .build();
    }

    // ── Private helpers ──────────────────────────────────────

    private long count(List<Ticket> tickets, String status) {
        return tickets.stream().filter(t -> status.equals(t.getCurrentStatus())).count();
    }

    private long countMultiple(List<Ticket> tickets, String... statuses) {
        Set<String> set = Set.of(statuses);
        return tickets.stream().filter(t -> set.contains(t.getCurrentStatus())).count();
    }

    private Map<String, Long> groupBy(List<Ticket> tickets, java.util.function.Function<Ticket, String> keyFn) {
        return tickets.stream()
                .filter(t -> keyFn.apply(t) != null)
                .collect(Collectors.groupingBy(keyFn, Collectors.counting()));
    }

    private List<EmployeePerformanceDTO> buildPerformance(List<Ticket> tickets) {
        Map<Long, List<Ticket>> byEmp = tickets.stream()
                .filter(t -> t.getAssignedEmployee() != null)
                .collect(Collectors.groupingBy(t -> t.getAssignedEmployee().getId()));

        return byEmp.entrySet().stream().map(entry -> {
            Long empId = entry.getKey();
            List<Ticket> empTickets = entry.getValue();
            String empName = empTickets.get(0).getAssignedEmployee().getEmployeeName();
            String level   = empTickets.get(0).getAssignedEmployee().getSupportLevel();

            long res = empTickets.stream()
                    .filter(t -> "Resolved".equals(t.getCurrentStatus()) || "Closed".equals(t.getCurrentStatus()))
                    .count();
            long breach = empTickets.stream()
                    .filter(t -> Boolean.TRUE.equals(t.getSlaBreached()))
                    .count();
            long openCount = empTickets.stream()
                    .filter(t -> "Open".equals(t.getCurrentStatus()) || "In Progress".equals(t.getCurrentStatus()))
                    .count();

            OptionalDouble avgHours = empTickets.stream()
                    .filter(t -> t.getBusinessHoursElapsed() != null && t.getBusinessHoursElapsed() > 0)
                    .mapToInt(Ticket::getBusinessHoursElapsed)
                    .average();

            return EmployeePerformanceDTO.builder()
                    .employeeId(empId)
                    .employeeName(empName)
                    .supportLevel(level)
                    .totalAssigned((long) empTickets.size())
                    .resolved(res)
                    .open(openCount)
                    .inProgress(0L)
                    .slaBreached(breach)
                    .avgResolutionHours(avgHours.isPresent()
                            ? Math.round(avgHours.getAsDouble() * 10.0) / 10.0 : null)
                    .build();
        }).collect(Collectors.toList());
    }
}