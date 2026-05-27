package com.ticketsystem.service;

import com.ticketsystem.dto.TicketDTO;
import com.ticketsystem.dto.TicketFilterDTO;
import com.ticketsystem.entity.Employee;
import com.ticketsystem.entity.Project;
import com.ticketsystem.entity.SlaConfig;
import com.ticketsystem.entity.Ticket;
import com.ticketsystem.exception.ResourceNotFoundException;
import com.ticketsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository      ticketRepository;
    private final ProjectRepository     projectRepository;
    private final EmployeeRepository    employeeRepository;
    private final SlaConfigRepository   slaConfigRepository;
    private final BusinessHoursCalculator bh;

    // ── Read ─────────────────────────────────────────────────

    public Page<TicketDTO> getAllTickets(TicketFilterDTO filter, Pageable pageable) {
        return ticketRepository
                .findAll(TicketSpecification.withFilters(filter), pageable)
                .map(this::toDTO);
    }

    public TicketDTO getTicketById(Long id) {
        return toDTO(findById(id));
    }

    public TicketDTO getTicketByNumber(String ticketNumber) {
        return toDTO(ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketNumber)));
    }

    // ── Write ────────────────────────────────────────────────

    @Transactional
    public TicketDTO createTicket(TicketDTO dto) {
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + dto.getProjectId()));

        Employee employee = resolveEmployee(dto.getAssignedEmployeeId());

        LocalDateTime generation = dto.getGenerationDatetime() != null
                ? dto.getGenerationDatetime() : LocalDateTime.now();

        String resolutionTime = null;
        int    businessHours  = 0;
        if (dto.getResponseDatetime() != null) {
            businessHours  = bh.calculateBusinessHours(generation, dto.getResponseDatetime());
            resolutionTime = bh.formatDuration(generation, dto.getResponseDatetime());
        }

        boolean slaBreached = computeSlaBreached(
                project, dto.getPriority(), generation,
                dto.getResponseDatetime(), businessHours);

        Ticket ticket = Ticket.builder()
                .ticketNumber(generateTicketNumber())
                .project(project)
                .issueDescription(dto.getIssueDescription().trim())
                .assignedEmployee(employee)
                .supportLevel(dto.getSupportLevel())
                .priority(dto.getPriority())
                .generationDatetime(generation)
                .responseDatetime(dto.getResponseDatetime())
                .resolutionTime(resolutionTime)
                .businessHoursElapsed(businessHours)
                .currentStatus(dto.getCurrentStatus() != null ? dto.getCurrentStatus() : "Open")
                .resolutionDetails(dto.getResolutionDetails())
                .remarks(dto.getRemarks())
                .slaBreached(slaBreached)
                .build();

        return toDTO(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketDTO updateTicket(Long id, TicketDTO dto) {
        Ticket ticket = findById(id);

        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + dto.getProjectId()));

        Employee employee = resolveEmployee(dto.getAssignedEmployeeId());

        String resolutionTime = null;
        int    businessHours  = ticket.getBusinessHoursElapsed() != null
                                ? ticket.getBusinessHoursElapsed() : 0;

        LocalDateTime generation = ticket.getGenerationDatetime();

        if (dto.getResponseDatetime() != null) {
            businessHours  = bh.calculateBusinessHours(generation, dto.getResponseDatetime());
            resolutionTime = bh.formatDuration(generation, dto.getResponseDatetime());
        }

        boolean slaBreached = computeSlaBreached(
                project, dto.getPriority(), generation,
                dto.getResponseDatetime(), businessHours);

        ticket.setProject(project);
        ticket.setIssueDescription(dto.getIssueDescription().trim());
        ticket.setAssignedEmployee(employee);
        ticket.setSupportLevel(dto.getSupportLevel());
        ticket.setPriority(dto.getPriority());
        ticket.setResponseDatetime(dto.getResponseDatetime());
        ticket.setResolutionTime(resolutionTime);
        ticket.setBusinessHoursElapsed(businessHours);
        ticket.setCurrentStatus(dto.getCurrentStatus());
        ticket.setResolutionDetails(dto.getResolutionDetails());
        ticket.setRemarks(dto.getRemarks());
        ticket.setSlaBreached(slaBreached);

        return toDTO(ticketRepository.save(ticket));
    }

    @Transactional
    public void deleteTicket(Long id) {
        ticketRepository.delete(findById(id));
    }

    // ── SLA Business Logic ───────────────────────────────────

    /**
     * Determine if SLA has been breached.
     * Uses per-project SLA config when available; falls back to project.slaHours.
     */
    private boolean computeSlaBreached(Project project, String priority,
                                       LocalDateTime generation,
                                       LocalDateTime resolution,
                                       int businessHoursElapsed) {
        if (generation == null) return false;

        int slaWindow = getSlaHours(project, priority);
        LocalDateTime reference = (resolution != null) ? resolution : LocalDateTime.now();
        int elapsed = (businessHoursElapsed > 0)
                ? businessHoursElapsed
                : bh.calculateBusinessHours(generation, reference);

        return elapsed > slaWindow;
    }

    private int getSlaHours(Project project, String priority) {
        // Try project-specific SLA config first
        if (priority != null && !priority.isBlank()) {
            return slaConfigRepository
                    .findByProjectIdAndPriorityLevel(project.getId(), priority)
                    .map(SlaConfig::getResolutionTimeSla)
                    .orElse(project.getSlaHours() != null ? project.getSlaHours() : 24);
        }
        return project.getSlaHours() != null ? project.getSlaHours() : 24;
    }

    // ── Helpers ──────────────────────────────────────────────

    private Employee resolveEmployee(Long id) {
        if (id == null) return null;
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
    }

    private Ticket findById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
    }

    private synchronized String generateTicketNumber() {
        Integer max = ticketRepository.findMaxTicketSequence();
        int next = (max == null ? 1000 : max) + 1;
        return "INC-" + next;
    }

    public TicketDTO toDTO(Ticket t) {
        int slaHours = t.getProject() != null
                ? getSlaHours(t.getProject(), t.getPriority())
                : 24;

        // Live SLA remaining (for open tickets)
        int elapsedHours = t.getBusinessHoursElapsed() != null
                ? t.getBusinessHoursElapsed()
                : (t.getGenerationDatetime() != null
                   ? bh.calculateBusinessHours(t.getGenerationDatetime(), LocalDateTime.now())
                   : 0);

        int slaRemaining = slaHours - elapsedHours;

        return TicketDTO.builder()
                .id(t.getId())
                .ticketNumber(t.getTicketNumber())
                .projectId(t.getProject()   != null ? t.getProject().getId()   : null)
                .projectName(t.getProject() != null ? t.getProject().getProjectName() : null)
                .issueDescription(t.getIssueDescription())
                .assignedEmployeeId(t.getAssignedEmployee()   != null ? t.getAssignedEmployee().getId()   : null)
                .assignedEmployeeName(t.getAssignedEmployee() != null ? t.getAssignedEmployee().getEmployeeName() : null)
                .supportLevel(t.getSupportLevel())
                .priority(t.getPriority())
                .generationDatetime(t.getGenerationDatetime())
                .responseDatetime(t.getResponseDatetime())
                .resolutionTime(t.getResolutionTime())
                .businessHoursElapsed(elapsedHours)
                .currentStatus(t.getCurrentStatus())
                .resolutionDetails(t.getResolutionDetails())
                .remarks(t.getRemarks())
                .slaBreached(t.getSlaBreached())
                .slaRemainingHours(slaRemaining)
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}