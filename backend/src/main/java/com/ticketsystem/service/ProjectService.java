package com.ticketsystem.service;

import com.ticketsystem.dto.ProjectDTO;
import com.ticketsystem.entity.Project;
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

    public List<ProjectDTO> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ProjectDTO getProjectById(Long id) {
        return toDTO(findById(id));
    }

    @Transactional
    public ProjectDTO createProject(ProjectDTO dto) {
        Project project = Project.builder()
                .projectName(dto.getProjectName().trim())
                .build();
        return toDTO(projectRepository.save(project));
    }

    @Transactional
    public ProjectDTO updateProject(Long id, ProjectDTO dto) {
        Project project = findById(id);
        project.setProjectName(dto.getProjectName().trim());
        return toDTO(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(Long id) {
        projectRepository.delete(findById(id));
    }

    private Project findById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
    }

    private ProjectDTO toDTO(Project p) {
        return ProjectDTO.builder()
                .id(p.getId())
                .projectName(p.getProjectName())
                .build();
    }
}
