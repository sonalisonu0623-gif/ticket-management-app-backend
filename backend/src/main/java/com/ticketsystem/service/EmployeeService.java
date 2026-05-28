package com.ticketsystem.service;

import com.ticketsystem.dto.EmployeeDTO;
import com.ticketsystem.entity.Employee;
import com.ticketsystem.entity.Project;
import com.ticketsystem.exception.DuplicateResourceException;
import com.ticketsystem.exception.ResourceNotFoundException;
import com.ticketsystem.repository.EmployeeRepository;
import com.ticketsystem.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final ProjectRepository  projectRepository;

    // ── Read ─────────────────────────────────────────────────

    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public EmployeeDTO getEmployeeById(Long id) {
        return toDTO(findById(id));
    }

    public List<EmployeeDTO> getEmployeesByProject(Long projectId) {
        return employeeRepository.findByProjectId(projectId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<EmployeeDTO> getEmployeesByProjectAndLevel(Long projectId, String level) {
        return employeeRepository.findByProjectIdAndSupportLevel(projectId, level).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── Write ────────────────────────────────────────────────

    @Transactional
    public EmployeeDTO createEmployee(EmployeeDTO dto) {
        if (dto.getEmail() != null && !dto.getEmail().isBlank()
                && employeeRepository.existsByEmail(dto.getEmail().toLowerCase())) {
            throw new DuplicateResourceException("Email already registered: " + dto.getEmail());
        }

        Employee emp = Employee.builder()
                .employeeName(dto.getEmployeeName().trim())
                .email(dto.getEmail() != null ? dto.getEmail().trim().toLowerCase() : null)
                .supportLevel(dto.getSupportLevel())
                .role(dto.getRole() != null ? dto.getRole() : "L1_SUPPORT")
                .designation(dto.getDesignation())
                .shift(dto.getShift())
                .status(dto.getStatus() != null ? dto.getStatus() : "ACTIVE")
                .build();

        // Auto-generate employeeId if not supplied
        emp.setEmployeeId(dto.getEmployeeId() != null && !dto.getEmployeeId().isBlank()
                ? dto.getEmployeeId() : generateEmployeeId());

        // Assign projects
        if (dto.getProjectIds() != null && !dto.getProjectIds().isEmpty()) {
            Set<Project> projects = dto.getProjectIds().stream()
                    .map(pid -> projectRepository.findById(pid)
                            .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + pid)))
                    .collect(Collectors.toSet());
            emp.setProjects(projects);
        }

        return toDTO(employeeRepository.save(emp));
    }

    @Transactional
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO dto) {
        Employee emp = findById(id);

        // Email uniqueness (exclude self)
        if (dto.getEmail() != null && !dto.getEmail().isBlank()
                && !dto.getEmail().equalsIgnoreCase(emp.getEmail())
                && employeeRepository.existsByEmail(dto.getEmail().toLowerCase())) {
            throw new DuplicateResourceException("Email already registered: " + dto.getEmail());
        }

        emp.setEmployeeName(dto.getEmployeeName().trim());
        if (dto.getEmail()       != null) emp.setEmail(dto.getEmail().trim().toLowerCase());
        if (dto.getSupportLevel()!= null) emp.setSupportLevel(dto.getSupportLevel());
        if (dto.getRole()        != null) emp.setRole(dto.getRole());
        if (dto.getDesignation() != null) emp.setDesignation(dto.getDesignation());
        if (dto.getShift()       != null) emp.setShift(dto.getShift());
        if (dto.getStatus()      != null) emp.setStatus(dto.getStatus());

        if (dto.getProjectIds() != null) {
            Set<Project> projects = dto.getProjectIds().stream()
                    .map(pid -> projectRepository.findById(pid)
                            .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + pid)))
                    .collect(Collectors.toSet());
            emp.setProjects(projects);
        }

        return toDTO(employeeRepository.save(emp));
    }

    @Transactional
    public void deleteEmployee(Long id) {
        employeeRepository.delete(findById(id));
    }

    @Transactional
    public EmployeeDTO assignToProject(Long employeeId, Long projectId) {
        Employee emp     = findById(employeeId);
        Project  project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        emp.getProjects().add(project);
        return toDTO(employeeRepository.save(emp));
    }

    @Transactional
    public EmployeeDTO removeFromProject(Long employeeId, Long projectId) {
        Employee emp = findById(employeeId);
        emp.getProjects().removeIf(p -> p.getId().equals(projectId));
        return toDTO(employeeRepository.save(emp));
    }

    // ── Helpers ──────────────────────────────────────────────

    public Employee findById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    private synchronized String generateEmployeeId() {
        long count = employeeRepository.count() + 1;
        return "EMP-" + String.format("%04d", count);
    }

    public EmployeeDTO toDTO(Employee e) {
        List<Long>   projectIds   = e.getProjects().stream().map(Project::getId).collect(Collectors.toList());
        List<String> projectNames = e.getProjects().stream().map(Project::getProjectName).collect(Collectors.toList());

        return EmployeeDTO.builder()
                .id(e.getId())
                .employeeId(e.getEmployeeId())
                .employeeName(e.getEmployeeName())
                .email(e.getEmail())
                .supportLevel(e.getSupportLevel())
                .role(e.getRole())
                .designation(e.getDesignation())
                .shift(e.getShift())
                .status(e.getStatus())
                .projectIds(projectIds)
                .projectNames(projectNames)
                .createdAt(e.getCreatedAt())
                .build();
    }
}
