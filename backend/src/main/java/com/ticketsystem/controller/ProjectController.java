package com.ticketsystem.controller;

import com.ticketsystem.dto.ApiResponse;
import com.ticketsystem.dto.ProjectDTO;
import com.ticketsystem.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /** Any authenticated user can list projects (filtered by their access in the UI) */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectDTO>>> getAll(
            @RequestParam(required = false) String status) {
        List<ProjectDTO> projects = (status != null && status.equalsIgnoreCase("ACTIVE"))
                ? projectService.getActiveProjects()
                : projectService.getAllProjects();
        return ResponseEntity.ok(ApiResponse.success("Projects fetched", projects));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Project fetched",
                projectService.getProjectById(id)));
    }

    /** Projects for a specific employee (used by non-admin users) */
    @GetMapping("/by-employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<ProjectDTO>>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(ApiResponse.success("Projects fetched",
                projectService.getProjectsByEmployee(employeeId)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProjectDTO>> create(@Valid @RequestBody ProjectDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project created", projectService.createProject(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProjectDTO>> update(
            @PathVariable Long id, @Valid @RequestBody ProjectDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Project updated",
                projectService.updateProject(id, dto)));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProjectDTO>> activate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Project activated",
                projectService.activateProject(id)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProjectDTO>> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Project deactivated",
                projectService.deactivateProject(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.success("Project deleted", null));
    }
}