package com.ticketsystem.service;

import com.ticketsystem.dto.TicketDTO;
import com.ticketsystem.dto.TicketFilterDTO;
import com.ticketsystem.entity.Employee;
import com.ticketsystem.entity.Project;
import com.ticketsystem.entity.Ticket;
import com.ticketsystem.exception.ResourceNotFoundException;
import com.ticketsystem.repository.EmployeeRepository;
import com.ticketsystem.repository.ProjectRepository;
import com.ticketsystem.repository.TicketRepository;
import com.ticketsystem.repository.TicketSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;

    public Page<TicketDTO> getAllTickets(TicketFilterDTO filter, Pageable pageable) {
        return ticketRepository.findAll(TicketSpecification.withFilters(filter), pageable)
                .map(this::toDTO);
    }

    public TicketDTO getTicketById(Long id) {
        return toDTO(findById(id));
    }

    public TicketDTO getTicketByNumber(String ticketNumber) {
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketNumber));
        return toDTO(ticket);
    }

    public TicketDTO createTicket(TicketDTO dto) {
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        Employee employee = null;
        if (dto.getAssignedEmployeeId() != null) {
            employee = employeeRepository.findById(dto.getAssignedEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        }

        String ticketNumber = generateTicketNumber();
        LocalDateTime generationTime = LocalDateTime.now();
        String resolutionTime = calculateResolutionTime(generationTime, dto.getResponseDatetime());

        Ticket ticket = Ticket.builder()
                .ticketNumber(ticketNumber)
                .project(project)
                .issueDescription(dto.getIssueDescription())
                .assignedEmployee(employee)
                .supportLevel(dto.getSupportLevel())
                .priority(dto.getPriority())
                .generationDatetime(generationTime)
                .responseDatetime(dto.getResponseDatetime())
                .resolutionTime(resolutionTime)
                .currentStatus(dto.getCurrentStatus() != null ? dto.getCurrentStatus() : "Open")
                .resolutionDetails(dto.getResolutionDetails())
                .remarks(dto.getRemarks())
                .build();

        return toDTO(ticketRepository.save(ticket));
    }

    public TicketDTO updateTicket(Long id, TicketDTO dto) {
        Ticket ticket = findById(id);

        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        Employee employee = null;
        if (dto.getAssignedEmployeeId() != null) {
            employee = employeeRepository.findById(dto.getAssignedEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        }

        String resolutionTime = calculateResolutionTime(ticket.getGenerationDatetime(), dto.getResponseDatetime());

        ticket.setProject(project);
        ticket.setIssueDescription(dto.getIssueDescription());
        ticket.setAssignedEmployee(employee);
        ticket.setSupportLevel(dto.getSupportLevel());
        ticket.setPriority(dto.getPriority());
        ticket.setResponseDatetime(dto.getResponseDatetime());
        ticket.setResolutionTime(resolutionTime);
        ticket.setCurrentStatus(dto.getCurrentStatus());
        ticket.setResolutionDetails(dto.getResolutionDetails());
        ticket.setRemarks(dto.getRemarks());

        return toDTO(ticketRepository.save(ticket));
    }

    public void deleteTicket(Long id) {
        ticketRepository.delete(findById(id));
    }

    private String generateTicketNumber() {
        Integer maxSeq = ticketRepository.findMaxTicketSequence();
        int next = (maxSeq == null ? 1000 : maxSeq) + 1;
        return "INC-" + next;
    }

    private String calculateResolutionTime(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return null;
        Duration duration = Duration.between(start, end);
        if (duration.isNegative()) return "N/A";
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return hours + "h " + minutes + "m";
    }

    private Ticket findById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
    }

    private TicketDTO toDTO(Ticket t) {
        return TicketDTO.builder()
                .id(t.getId())
                .ticketNumber(t.getTicketNumber())
                .projectId(t.getProject() != null ? t.getProject().getId() : null)
                .projectName(t.getProject() != null ? t.getProject().getProjectName() : null)
                .issueDescription(t.getIssueDescription())
                .assignedEmployeeId(t.getAssignedEmployee() != null ? t.getAssignedEmployee().getId() : null)
                .assignedEmployeeName(t.getAssignedEmployee() != null ? t.getAssignedEmployee().getEmployeeName() : null)
                .supportLevel(t.getSupportLevel())
                .priority(t.getPriority())
                .generationDatetime(t.getGenerationDatetime())
                .responseDatetime(t.getResponseDatetime())
                .resolutionTime(t.getResolutionTime())
                .currentStatus(t.getCurrentStatus())
                .resolutionDetails(t.getResolutionDetails())
                .remarks(t.getRemarks())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
