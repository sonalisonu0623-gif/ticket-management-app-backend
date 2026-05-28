package com.ticketsystem.controller;

import com.ticketsystem.dto.ApiResponse;
import com.ticketsystem.dto.DashboardStatsDTO;
import com.ticketsystem.dto.TicketRequestDTO;
import com.ticketsystem.dto.TicketResponseDTO;
import com.ticketsystem.entity.Ticket.CurrentStatus;
import com.ticketsystem.entity.Ticket.Priority;
import com.ticketsystem.repository.EmployeeRepository;
import com.ticketsystem.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class TicketController {

    private final TicketService      ticketService;
    private final EmployeeRepository employeeRepository;  // merged entity — UserRepository removed

    // POST /api/tickets
    @PostMapping
    public ResponseEntity<ApiResponse<TicketResponseDTO>> createTicket(
            @Valid @RequestBody TicketRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ticket created successfully",
                        ticketService.createTicket(requestDTO)));
    }

    // GET /api/tickets
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TicketResponseDTO>>> getAllTickets(
            @RequestParam(defaultValue = "0")        int page,
            @RequestParam(defaultValue = "10")       int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc")     String sortDir) {

        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Tickets retrieved successfully",
                ticketService.getAllTickets(pageable)));
    }

    // GET /api/tickets/dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getDashboardStats() {
        return ResponseEntity.ok(ApiResponse.success("Dashboard stats retrieved",
                ticketService.getDashboardStats()));
    }

    // GET /api/tickets/search
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<TicketResponseDTO>>> searchTickets(
            @RequestParam(required = false) String ticketId,
            @RequestParam(required = false) String projectAssignment,
            @RequestParam(required = false) CurrentStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(defaultValue = "0")        int page,
            @RequestParam(defaultValue = "10")       int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc")     String sortDir) {

        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Search results retrieved",
                ticketService.searchTickets(ticketId, projectAssignment, status, priority, pageable)));
    }

    // GET /api/tickets/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketResponseDTO>> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Ticket retrieved successfully",
                ticketService.getTicketById(id)));
    }

    // PUT /api/tickets/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketResponseDTO>> updateTicket(
            @PathVariable Long id,
            @Valid @RequestBody TicketRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponse.success("Ticket updated successfully",
                ticketService.updateTicket(id, requestDTO)));
    }

    // DELETE /api/tickets/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.ok(ApiResponse.success("Ticket deleted successfully", null));
    }

    // GET /api/tickets/my-tickets — tickets assigned to the logged-in employee
    @GetMapping("/my-tickets")
    public ResponseEntity<ApiResponse<Page<TicketResponseDTO>>> getMyTickets(
            Principal principal,
            @RequestParam(required = false) CurrentStatus status,
            @RequestParam(defaultValue = "0")        int page,
            @RequestParam(defaultValue = "10")       int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc")     String sortDir) {

        // With the merged Employee entity, principal.getName() is the username.
        // Resolve it directly to employeeName (what tickets are assigned against).
        String assigneeName = principal == null ? "" :
                employeeRepository.findByUsername(principal.getName())
                        .map(emp -> emp.getEmployeeName())
                        .orElse(principal.getName());   // fallback: use username as-is

        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("My tickets retrieved",
                ticketService.getTicketsByAssignee(assigneeName, status, pageable)));
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = "asc".equalsIgnoreCase(sortDir)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return PageRequest.of(page, size, sort);
    }
}
