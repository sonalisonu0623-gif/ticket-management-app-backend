package com.ticketsystem.serviceImpl;

import com.ticketsystem.dto.DashboardStatsDTO;
import com.ticketsystem.dto.TicketRequestDTO;
import com.ticketsystem.dto.TicketResponseDTO;
import com.ticketsystem.entity.Ticket;
import com.ticketsystem.entity.Ticket.CurrentStatus;
import com.ticketsystem.entity.Ticket.Priority;
import com.ticketsystem.exception.ResourceNotFoundException;
import com.ticketsystem.repository.SlaConfigRepository;
import com.ticketsystem.repository.TicketRepository;
import com.ticketsystem.service.SlaCalculationService;
import com.ticketsystem.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {

    private final TicketRepository      ticketRepository;
    private final SlaCalculationService slaCalculationService;
    private final SlaConfigRepository   slaConfigRepository;

    @Override
    @Transactional
    public TicketResponseDTO createTicket(TicketRequestDTO requestDTO) {
        log.info("Creating ticket for project: {}", requestDTO.getProjectAssignment());

        String ticketId = generateTicketId();

        Ticket ticket = Ticket.builder()
                .ticketId(ticketId)
                .projectAssignment(requestDTO.getProjectAssignment())
                .issueDescription(requestDTO.getIssueDescription())
                .assignedEmployee(requestDTO.getAssignedEmployee())
                .supportLevel(requestDTO.getSupportLevel())
                .priority(requestDTO.getPriority())
                .generationDateTime(requestDTO.getGenerationDateTime() != null
                        ? requestDTO.getGenerationDateTime() : LocalDateTime.now())
                .responseDateTime(requestDTO.getResponseDateTime())
                .resolutionTime(requestDTO.getResolutionTime())
                .currentStatus(requestDTO.getCurrentStatus() != null
                        ? requestDTO.getCurrentStatus() : CurrentStatus.OPEN)
                .resolutionDetails(requestDTO.getResolutionDetails())
                .remarks(requestDTO.getRemarks())
                .build();

        Ticket saved = ticketRepository.save(ticket);
        log.info("Ticket created: {}", saved.getTicketId());
        return mapToResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponseDTO> getAllTickets(Pageable pageable) {
        return ticketRepository.findAll(pageable).map(this::mapToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponseDTO getTicketById(Long id) {
        return mapToResponseDTO(ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id)));
    }

    @Override
    @Transactional
    public TicketResponseDTO updateTicket(Long id, TicketRequestDTO requestDTO) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id));

        ticket.setProjectAssignment(requestDTO.getProjectAssignment());
        ticket.setIssueDescription(requestDTO.getIssueDescription());
        ticket.setAssignedEmployee(requestDTO.getAssignedEmployee());
        ticket.setSupportLevel(requestDTO.getSupportLevel());
        ticket.setPriority(requestDTO.getPriority());
        ticket.setGenerationDateTime(requestDTO.getGenerationDateTime());
        ticket.setResponseDateTime(requestDTO.getResponseDateTime());
        ticket.setResolutionTime(requestDTO.getResolutionTime());
        ticket.setCurrentStatus(requestDTO.getCurrentStatus());
        ticket.setResolutionDetails(requestDTO.getResolutionDetails());
        ticket.setRemarks(requestDTO.getRemarks());

        return mapToResponseDTO(ticketRepository.save(ticket));
    }

    @Override
    @Transactional
    public void deleteTicket(Long id) {
        ticketRepository.delete(ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponseDTO> searchTickets(
            String ticketId, String projectAssignment,
            CurrentStatus status, Priority priority, Pageable pageable) {
        return ticketRepository
                .searchTickets(ticketId, projectAssignment, status, priority, pageable)
                .map(this::mapToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats() {
        long total      = ticketRepository.count();
        long open       = ticketRepository.countByCurrentStatus(CurrentStatus.OPEN);
        long inProgress = ticketRepository.countByCurrentStatus(CurrentStatus.IN_PROGRESS);
        long resolved   = ticketRepository.countByCurrentStatus(CurrentStatus.RESOLVED);
        long closed     = ticketRepository.countByCurrentStatus(CurrentStatus.CLOSED);

        long slaBreached = ticketRepository.findAll().stream()
                .filter(t -> t.getCurrentStatus() == CurrentStatus.OPEN
                          || t.getCurrentStatus() == CurrentStatus.IN_PROGRESS)
                .filter(t -> {
                    try {
                        return slaCalculationService.isSlaBreached(
                                t.getPriority().name(),
                                t.getSupportLevel().name(),
                                t.getGenerationDateTime() != null
                                        ? t.getGenerationDateTime() : t.getCreatedAt(),
                                t.getAssignedEmployee());
                    } catch (Exception e) { return false; }
                })
                .count();

        return DashboardStatsDTO.builder()
                .totalTickets(total)
                .openTickets(open)
                .inProgressTickets(inProgress)
                .resolvedTickets(resolved)
                .closedTickets(closed)
                .slaBreachedTickets(slaBreached)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponseDTO> getTicketsByAssignee(
            String assignedEmployee, CurrentStatus status, Pageable pageable) {
        return ticketRepository.findByAssignedEmployee(assignedEmployee, status, pageable)
                .map(this::mapToResponseDTO);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String generateTicketId() {
        Integer maxSeq = ticketRepository.findMaxTicketSequence();
        int nextSeq = (maxSeq == null ? 1000 : maxSeq) + 1;
        String ticketId = "INC-" + nextSeq;
        while (ticketRepository.existsByTicketId(ticketId)) {
            nextSeq++;
            ticketId = "INC-" + nextSeq;
        }
        return ticketId;
    }

    private TicketResponseDTO mapToResponseDTO(Ticket ticket) {
        TicketResponseDTO dto = TicketResponseDTO.builder()
                .id(ticket.getId())
                .ticketId(ticket.getTicketId())
                .projectAssignment(ticket.getProjectAssignment())
                .issueDescription(ticket.getIssueDescription())
                .assignedEmployee(ticket.getAssignedEmployee())
                .supportLevel(ticket.getSupportLevel())
                .priority(ticket.getPriority())
                .generationDateTime(ticket.getGenerationDateTime())
                .responseDateTime(ticket.getResponseDateTime())
                .resolutionTime(ticket.getResolutionTime())
                .currentStatus(ticket.getCurrentStatus())
                .resolutionDetails(ticket.getResolutionDetails())
                .remarks(ticket.getRemarks())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();

        // ── Enrich with SLA data, using assigned employee's shift ─────────────
        try {
            LocalDateTime createdAt = ticket.getGenerationDateTime() != null
                    ? ticket.getGenerationDateTime() : ticket.getCreatedAt();

            if (createdAt != null && ticket.getPriority() != null && ticket.getSupportLevel() != null) {
                String priorityName = ticket.getPriority().name();
                String levelName    = ticket.getSupportLevel().name();
                String assignee     = ticket.getAssignedEmployee();   // may be null

                slaConfigRepository.findByPriorityAndSupportLevel(priorityName, levelName)
                        .ifPresent(cfg -> {
                            // Pass assignee so the employee's specific shift is used
                            LocalDateTime deadline = slaCalculationService
                                    .addWorkingHours(createdAt, cfg.getResolutionTimeHours(), assignee);
                            dto.setSlaDeadline(deadline);

                            boolean active = ticket.getCurrentStatus() != Ticket.CurrentStatus.RESOLVED
                                          && ticket.getCurrentStatus() != Ticket.CurrentStatus.CLOSED;
                            dto.setSlaBreached(active && LocalDateTime.now().isAfter(deadline));

                            long elapsed = slaCalculationService
                                    .workingMinutesBetween(createdAt, LocalDateTime.now(), assignee) / 60;
                            dto.setWorkingHoursElapsed(elapsed);
                        });
            }
        } catch (Exception e) {
            log.debug("SLA enrichment skipped for ticket {}: {}", ticket.getTicketId(), e.getMessage());
        }

        return dto;
    }
}
