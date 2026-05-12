package com.ticketsystem.service;

import com.ticketsystem.dto.request.TicketRequest;
import com.ticketsystem.dto.response.TicketResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

public interface TicketService {

    TicketResponse createTicket(TicketRequest request, String username);

    TicketResponse updateTicket(Long id, TicketRequest request, String username);

    TicketResponse getTicketById(Long id);

    TicketResponse getTicketByNumber(String ticketNumber);

    Page<TicketResponse> getAllTickets(Pageable pageable);

    Page<TicketResponse> getTicketsWithFilters(
            String ticketNumber, Long projectId, Long assignedToId,
            String priority, String status, String supportLevel,
            LocalDate startDate, LocalDate endDate,
            String search, Pageable pageable
    );

    Page<TicketResponse> getTicketsByUser(String username, Pageable pageable);

    Page<TicketResponse> getTicketsAssignedToUser(String username, Pageable pageable);

    void deleteTicket(Long id, String username);

    TicketResponse assignTicket(Long ticketId, Long userId, String username);

    TicketResponse updateTicketStatus(Long ticketId, String status, String resolutionDetails, String username);

    TicketResponse addComment(Long ticketId, String comment, boolean isInternal, String username);

    TicketResponse uploadAttachment(Long ticketId, MultipartFile file, String username);

    ByteArrayOutputStream exportToExcel(String status, String priority);

    ByteArrayOutputStream exportToPdf(String status, String priority);
}
