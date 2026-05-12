package com.ticketsystem.service.impl;

import com.ticketsystem.dto.request.ProjectRequest;
import com.ticketsystem.dto.response.ProjectResponse;
import com.ticketsystem.entity.Project;
import com.ticketsystem.exception.DuplicateResourceException;
import com.ticketsystem.exception.ResourceNotFoundException;
import com.ticketsystem.repository.ProjectRepository;
import com.ticketsystem.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl {

    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getActiveProjects() {
        return projectRepository.findByIsActive(true).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {
        return mapToResponse(projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id)));
    }

    public ProjectResponse createProject(ProjectRequest request) {
        if (projectRepository.existsByName(request.getName()))
            throw new DuplicateResourceException("Project name already exists: " + request.getName());
        if (projectRepository.existsByProjectCode(request.getProjectCode()))
            throw new DuplicateResourceException("Project code already exists: " + request.getProjectCode());

        Project project = Project.builder()
                .name(request.getName()).description(request.getDescription())
                .projectCode(request.getProjectCode()).isActive(request.getIsActive())
                .build();
        return mapToResponse(projectRepository.save(project));
    }

    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setIsActive(request.getIsActive());
        return mapToResponse(projectRepository.save(project));
    }

    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) throw new ResourceNotFoundException("Project", id);
        projectRepository.deleteById(id);
    }

    private ProjectResponse mapToResponse(Project p) {
        return ProjectResponse.builder()
                .id(p.getId()).name(p.getName()).description(p.getDescription())
                .projectCode(p.getProjectCode()).isActive(p.getIsActive())
                .createdAt(p.getCreatedAt())
                .ticketCount((long) p.getTickets().size())
                .build();
    }
}
