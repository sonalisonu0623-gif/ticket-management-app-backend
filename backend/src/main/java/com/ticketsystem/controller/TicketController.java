package com.ticketsystem.controller;

import com.ticketsystem.dto.ApiResponse;
import com.ticketsystem.dto.TicketDTO;
import com.ticketsystem.dto.TicketFilterDTO;
import com.ticketsystem.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TicketDTO>>> getAll(
            @RequestParam(defaultValue = "0")   int    page,
            @RequestParam(defaultValue = "15")  int    size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String  ticketNumber,
            @RequestParam(required = false) Long    projectId,
            @RequestParam(required = false) Long    employeeId,
            @RequestParam(required = false) String  priority,
            @RequestParam(required = false) String  currentStatus,
            @RequestParam(required = false) String  supportLevel,
            @RequestParam(required = false) Boolean slaBreached,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        TicketFilterDTO filter = TicketFilterDTO.builder()
                .ticketNumber(ticketNumber)
                .projectId(projectId)
                .employeeId(employeeId)
                .priority(priority)
                .currentStatus(currentStatus)
                .supportLevel(supportLevel)
                .slaBreached(slaBreached)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .build();

        return ResponseEntity.ok(ApiResponse.success("Tickets fetched",
                ticketService.getAllTickets(filter, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Ticket fetched",
                ticketService.getTicketById(id)));
    }

    @GetMapping("/number/{ticketNumber}")
    public ResponseEntity<ApiResponse<TicketDTO>> getByNumber(@PathVariable String ticketNumber) {
        return ResponseEntity.ok(ApiResponse.success("Ticket fetched",
                ticketService.getTicketByNumber(ticketNumber)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TicketDTO>> create(@Valid @RequestBody TicketDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ticket created", ticketService.createTicket(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketDTO>> update(
            @PathVariable Long id, @Valid @RequestBody TicketDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Ticket updated",
                ticketService.updateTicket(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.ok(ApiResponse.success("Ticket deleted", null));
    }
}
