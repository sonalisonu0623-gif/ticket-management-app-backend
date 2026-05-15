package com.ticketsystem.controller;

import com.ticketsystem.dto.ApiResponse;
import com.ticketsystem.dto.ProjectDTO;
import com.ticketsystem.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Projects fetched", projectService.getAllProjects()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Project fetched", projectService.getProjectById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectDTO>> create(@Valid @RequestBody ProjectDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project created", projectService.createProject(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectDTO>> update(@PathVariable Long id, @Valid @RequestBody ProjectDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Project updated", projectService.updateProject(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.success("Project deleted", null));
    }
}
