package com.ticketsystem.service.impl;

import com.ticketsystem.dto.response.DashboardResponse;
import com.ticketsystem.entity.TicketStatus;
import com.ticketsystem.entity.Priority;
import com.ticketsystem.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl {

    private final TicketRepository ticketRepository;

    public DashboardResponse getDashboard() {
        Map<String, Long> statusDist = new LinkedHashMap<>();
        for (Object[] row : ticketRepository.getTicketStatusDistribution()) {
            statusDist.put(row[0].toString(), (Long) row[1]);
        }

        Map<String, Long> priorityDist = new LinkedHashMap<>();
        for (Object[] row : ticketRepository.getTicketPriorityDistribution()) {
            priorityDist.put(row[0].toString(), (Long) row[1]);
        }

        List<DashboardResponse.MonthlyTrendDto> trends = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yyyy");
        for (Object[] row : ticketRepository.getMonthlyTrends(LocalDateTime.now().minusMonths(12))) {
            int month = ((Number) row[0]).intValue();
            int year = ((Number) row[1]).intValue();
            Long count = (Long) row[2];
            LocalDateTime dt = LocalDateTime.of(year, month, 1, 0, 0);
            trends.add(DashboardResponse.MonthlyTrendDto.builder().month(dt.format(fmt)).count(count).build());
        }

        List<DashboardResponse.EmployeeWorkloadDto> workload = new ArrayList<>();
        for (Object[] row : ticketRepository.getEmployeeWorkload()) {
            workload.add(DashboardResponse.EmployeeWorkloadDto.builder()
                    .employeeName((String) row[0]).ticketCount((Long) row[1]).build());
        }

        return DashboardResponse.builder()
                .totalTickets(ticketRepository.count())
                .openTickets(ticketRepository.countByStatus(TicketStatus.OPEN))
                .inProgressTickets(ticketRepository.countByStatus(TicketStatus.IN_PROGRESS))
                .resolvedTickets(ticketRepository.countByStatus(TicketStatus.RESOLVED))
                .closedTickets(ticketRepository.countByStatus(TicketStatus.CLOSED))
                .criticalTickets(ticketRepository.countByPriority(Priority.P1_CRITICAL))
                .slaBreachedTickets(ticketRepository.countSlaBreached())
                .avgResolutionTimeMinutes(ticketRepository.getAverageResolutionTime())
                .statusDistribution(statusDist)
                .priorityDistribution(priorityDist)
                .monthlyTrends(trends)
                .employeeWorkload(workload)
                .build();
    }
}
