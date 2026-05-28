package com.ticketsystem.service;

import com.ticketsystem.dto.ApiResponse;
import com.ticketsystem.dto.DashboardStatsDTO;
import com.ticketsystem.dto.TicketRequestDTO;
import com.ticketsystem.dto.TicketResponseDTO;
import com.ticketsystem.entity.Ticket.CurrentStatus;
import com.ticketsystem.entity.Ticket.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TicketService {

    TicketResponseDTO createTicket(TicketRequestDTO requestDTO);

    Page<TicketResponseDTO> getAllTickets(Pageable pageable);

    TicketResponseDTO getTicketById(Long id);

    TicketResponseDTO updateTicket(Long id, TicketRequestDTO requestDTO);

    void deleteTicket(Long id);

    Page<TicketResponseDTO> searchTickets(
            String ticketId,
            String projectAssignment,
            CurrentStatus status,
            Priority priority,
            Pageable pageable
    );

    DashboardStatsDTO getDashboardStats();

    Page<TicketResponseDTO> getTicketsByAssignee(String assignedEmployee, CurrentStatus status, Pageable pageable);
}
