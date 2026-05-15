package com.ticketsystem.repository;

import com.ticketsystem.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    @Query("SELECT MAX(CAST(SUBSTRING(t.ticketNumber, 5) AS int)) FROM Ticket t WHERE t.ticketNumber LIKE 'INC-%'")
    Integer findMaxTicketSequence();
}
