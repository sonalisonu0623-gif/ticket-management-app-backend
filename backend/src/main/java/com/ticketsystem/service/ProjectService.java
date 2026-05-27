package com.ticketsystem.service;

import com.ticketsystem.dto.EmployeeDTO;
import com.ticketsystem.dto.ProjectDTO;
import com.ticketsystem.entity.Employee;
import com.ticketsystem.entity.Project;
import com.ticketsystem.exception.DuplicateResourceException;
import com.ticketsystem.exception.ResourceNotFoundException;
import com.ticketsystem.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;

    // ── Read ─────────────────────────────────────────────────

    public List<ProjectDTO> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ProjectDTO> getActiveProjects() {
        return projectRepository.findByStatusOrderByProjectNameAsc("ACTIVE").stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ProjectDTO getProjectById(Long id) {
        return toDTO(findById(id));
    }

    public List<ProjectDTO> getProjectsByEmployee(Long employeeId) {
        return projectRepository.findByEmployeeId(employeeId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── Write ────────────────────────────────────────────────

    @Transactional
    public ProjectDTO createProject(ProjectDTO dto) {
        if (projectRepository.existsByProjectName(dto.getProjectName().trim())) {
            throw new DuplicateResourceException("Project name already exists: " + dto.getProjectName());
        }
        if (dto.getProjectCode() != null && !dto.getProjectCode().isBlank()
                && projectRepository.existsByProjectCode(dto.getProjectCode().trim().toUpperCase())) {
            throw new DuplicateResourceException("Project code already exists: " + dto.getProjectCode());
        }

        Project project = Project.builder()
                .projectName(dto.getProjectName().trim())
                .projectCode(dto.getProjectCode() != null
                        ? dto.getProjectCode().trim().toUpperCase() : null)
                .description(dto.getDescription())
                .supportEmail(dto.getSupportEmail())
                .slaHours(dto.getSlaHours() != null ? dto.getSlaHours() : 24)
                .shiftTiming(dto.getShiftTiming())
                .status(dto.getStatus() != null ? dto.getStatus() : "ACTIVE")
                .build();

        return toDTO(projectRepository.save(project));
    }

    @Transactional
    public ProjectDTO updateProject(Long id, ProjectDTO dto) {
        Project project = findById(id);

        // Name uniqueness check (exclude self)
        if (!project.getProjectName().equalsIgnoreCase(dto.getProjectName().trim())
                && projectRepository.existsByProjectName(dto.getProjectName().trim())) {
            throw new DuplicateResourceException("Project name already taken: " + dto.getProjectName());
        }

        project.setProjectName(dto.getProjectName().trim());
        if (dto.getProjectCode() != null && !dto.getProjectCode().isBlank()) {
            project.setProjectCode(dto.getProjectCode().trim().toUpperCase());
        }
        if (dto.getDescription() != null)  project.setDescription(dto.getDescription());
        if (dto.getSupportEmail() != null) project.setSupportEmail(dto.getSupportEmail());
        if (dto.getSlaHours()    != null)  project.setSlaHours(dto.getSlaHours());
        if (dto.getShiftTiming() != null)  project.setShiftTiming(dto.getShiftTiming());
        if (dto.getStatus()      != null)  project.setStatus(dto.getStatus());

        return toDTO(projectRepository.save(project));
    }

    @Transactional
    public ProjectDTO activateProject(Long id) {
        Project p = findById(id);
        p.setStatus("ACTIVE");
        return toDTO(projectRepository.save(p));
    }

    @Transactional
    public ProjectDTO deactivateProject(Long id) {
        Project p = findById(id);
        p.setStatus("INACTIVE");
        return toDTO(projectRepository.save(p));
    }

    @Transactional
    public void deleteProject(Long id) {
        projectRepository.delete(findById(id));
    }

    // ── Helpers ──────────────────────────────────────────────

    public Project findById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
    }

    public ProjectDTO toDTO(Project p) {
        List<EmployeeDTO> empDTOs = p.getEmployees().stream()
                .map(e -> EmployeeDTO.builder()
                        .id(e.getId())
                        .employeeName(e.getEmployeeName())
                        .supportLevel(e.getSupportLevel())
                        .role(e.getRole())
                        .status(e.getStatus())
                        .build())
                .collect(Collectors.toList());

        return ProjectDTO.builder()
                .id(p.getId())
                .projectName(p.getProjectName())
                .projectCode(p.getProjectCode())
                .description(p.getDescription())
                .supportEmail(p.getSupportEmail())
                .slaHours(p.getSlaHours())
                .shiftTiming(p.getShiftTiming())
                .status(p.getStatus())
                .employees(empDTOs)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}