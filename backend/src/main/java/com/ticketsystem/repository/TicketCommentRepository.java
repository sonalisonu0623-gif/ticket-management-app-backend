package com.ticketsystem.repository;

import com.ticketsystem.entity.TicketComment;
import com.ticketsystem.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {
    List<TicketComment> findByTicketOrderByCreatedAtAsc(Ticket ticket);
    List<TicketComment> findByTicketAndIsInternalFalseOrderByCreatedAtAsc(Ticket ticket);
}
