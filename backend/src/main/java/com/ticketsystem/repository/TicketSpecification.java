package com.ticketsystem.repository;

import com.ticketsystem.dto.TicketFilterDTO;
import com.ticketsystem.entity.Ticket;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TicketSpecification {

    public static Specification<Ticket> withFilters(TicketFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Prevent duplicate rows from joins during paginated count query
            if (query != null && !query.getResultType().equals(Long.class)) {
                query.distinct(true);
            }

            // Ticket number (partial match)
            if (filter.getTicketNumber() != null && !filter.getTicketNumber().isBlank()) {
                predicates.add(cb.like(
                    cb.lower(root.get("ticketNumber")),
                    "%" + filter.getTicketNumber().toLowerCase() + "%"));
            }

            // Project filter
            if (filter.getProjectId() != null) {
                predicates.add(cb.equal(root.get("project").get("id"), filter.getProjectId()));
            }

            // Employee filter (LEFT JOIN so unassigned tickets are not dropped)
            if (filter.getEmployeeId() != null) {
                var empJoin = root.join("assignedEmployee", JoinType.LEFT);
                predicates.add(cb.equal(empJoin.get("id"), filter.getEmployeeId()));
            }

            // Priority exact match
            if (filter.getPriority() != null && !filter.getPriority().isBlank()) {
                predicates.add(cb.equal(root.get("priority"), filter.getPriority()));
            }

            // Status exact match
            if (filter.getCurrentStatus() != null && !filter.getCurrentStatus().isBlank()) {
                predicates.add(cb.equal(root.get("currentStatus"), filter.getCurrentStatus()));
            }

            // Support level exact match
            if (filter.getSupportLevel() != null && !filter.getSupportLevel().isBlank()) {
                predicates.add(cb.equal(root.get("supportLevel"), filter.getSupportLevel()));
            }

            // SLA breach flag
            if (filter.getSlaBreached() != null) {
                predicates.add(cb.equal(root.get("slaBreached"), filter.getSlaBreached()));
            }

            // Date range on createdAt
            if (filter.getDateFrom() != null) {
                LocalDateTime from = filter.getDateFrom().atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (filter.getDateTo() != null) {
                LocalDateTime to = filter.getDateTo().atTime(LocalTime.MAX);
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}