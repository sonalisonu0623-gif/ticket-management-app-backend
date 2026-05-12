package com.ticketsystem.repository;

import com.ticketsystem.entity.TicketHistory;
import com.ticketsystem.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Long> {
    List<TicketHistory> findByTicketOrderByChangedAtDesc(Ticket ticket);
}
