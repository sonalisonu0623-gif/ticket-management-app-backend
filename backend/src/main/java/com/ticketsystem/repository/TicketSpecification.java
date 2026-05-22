package com.ticketsystem.repository;

import com.ticketsystem.dto.TicketFilterDTO;
import com.ticketsystem.entity.Ticket;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TicketSpecification {

    public static Specification<Ticket> withFilters(TicketFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Avoid duplicate rows when joining for pagination
            if (query != null) query.distinct(true);

            if (filter.getTicketNumber() != null && !filter.getTicketNumber().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("ticketNumber")),
                        "%" + filter.getTicketNumber().toLowerCase() + "%"));
            }
            if (filter.getProjectId() != null) {
                predicates.add(cb.equal(root.get("project").get("id"), filter.getProjectId()));
            }
            if (filter.getEmployeeId() != null) {
                // LEFT JOIN so tickets with no assigned employee are not excluded
                var empJoin = root.join("assignedEmployee", JoinType.LEFT);
                predicates.add(cb.equal(empJoin.get("id"), filter.getEmployeeId()));
            }
            if (filter.getPriority() != null && !filter.getPriority().isBlank()) {
                predicates.add(cb.equal(root.get("priority"), filter.getPriority()));
            }
            if (filter.getCurrentStatus() != null && !filter.getCurrentStatus().isBlank()) {
                predicates.add(cb.equal(root.get("currentStatus"), filter.getCurrentStatus()));
            }
            if (filter.getSupportLevel() != null && !filter.getSupportLevel().isBlank()) {
                predicates.add(cb.equal(root.get("supportLevel"), filter.getSupportLevel()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
