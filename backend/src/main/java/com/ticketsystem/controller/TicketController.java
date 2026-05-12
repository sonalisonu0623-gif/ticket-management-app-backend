package com.ticketsystem.controller;

import com.ticketsystem.dto.request.TicketRequest;
import com.ticketsystem.dto.response.ApiResponse;
import com.ticketsystem.dto.response.TicketResponse;
import com.ticketsystem.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<ApiResponse<TicketResponse>> createTicket(
            @Valid @RequestBody TicketRequest request, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success("Ticket created successfully",
                ticketService.createTicket(request, principal.getName())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketResponse>> updateTicket(
            @PathVariable Long id,
            @Valid @RequestBody TicketRequest request,
            Principal principal) {
        return ResponseEntity.ok(ApiResponse.success("Ticket updated successfully",
                ticketService.updateTicket(id, request, principal.getName())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(ticketService.getTicketById(id)));
    }

    @GetMapping("/number/{ticketNumber}")
    public ResponseEntity<ApiResponse<TicketResponse>> getByNumber(@PathVariable String ticketNumber) {
        return ResponseEntity.ok(ApiResponse.success(ticketService.getTicketByNumber(ticketNumber)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT_ENGINEER')")
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> getAllTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String ticketNumber,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long assignedToId,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String supportLevel,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String search) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        Page<TicketResponse> tickets = ticketService.getTicketsWithFilters(
                ticketNumber, projectId, assignedToId, priority, status,
                supportLevel, startDate, endDate, search, pageable);

        return ResponseEntity.ok(ApiResponse.success(tickets));
    }

    @GetMapping("/my-tickets")
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> getMyTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {
        Page<TicketResponse> tickets = ticketService.getTicketsByUser(
                principal.getName(), PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(tickets));
    }

    @GetMapping("/assigned-to-me")
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> getAssignedTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {
        Page<TicketResponse> tickets = ticketService.getTicketsAssignedToUser(
                principal.getName(), PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(tickets));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTicket(@PathVariable Long id, Principal principal) {
        ticketService.deleteTicket(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Ticket deleted successfully", null));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT_ENGINEER')")
    public ResponseEntity<ApiResponse<TicketResponse>> assignTicket(
            @PathVariable Long id,
            @RequestParam Long userId,
            Principal principal) {
        return ResponseEntity.ok(ApiResponse.success("Ticket assigned",
                ticketService.assignTicket(id, userId, principal.getName())));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TicketResponse>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Principal principal) {
        return ResponseEntity.ok(ApiResponse.success("Status updated",
                ticketService.updateTicketStatus(id, body.get("status"),
                        body.get("resolutionDetails"), principal.getName())));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<TicketResponse>> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            Principal principal) {
        String content = (String) body.get("content");
        boolean isInternal = Boolean.TRUE.equals(body.get("isInternal"));
        return ResponseEntity.ok(ApiResponse.success("Comment added",
                ticketService.addComment(id, content, isInternal, principal.getName())));
    }

    @PostMapping("/{id}/attachments")
    public ResponseEntity<ApiResponse<TicketResponse>> uploadAttachment(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Principal principal) {
        return ResponseEntity.ok(ApiResponse.success("File uploaded",
                ticketService.uploadAttachment(id, file, principal.getName())));
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT_ENGINEER')")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority) throws Exception {
        ByteArrayOutputStream baos = ticketService.exportToExcel(status, priority);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "tickets.xlsx");
        return ResponseEntity.ok().headers(headers).body(baos.toByteArray());
    }
}
