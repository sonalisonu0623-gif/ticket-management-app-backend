package com.ticketsystem.controller;

import com.ticketsystem.dto.request.ProjectRequest;
import com.ticketsystem.dto.response.ApiResponse;
import com.ticketsystem.dto.response.ProjectResponse;
import com.ticketsystem.service.impl.ProjectServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectServiceImpl projectService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(projectService.getActiveProjects()));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getAllIncludingInactive() {
        return ResponseEntity.ok(ApiResponse.success(projectService.getAllProjects()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getProjectById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProjectResponse>> create(@Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Project created", projectService.createProject(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProjectResponse>> update(
            @PathVariable Long id, @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Project updated", projectService.updateProject(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.success("Project deleted", null));
    }
}
