package com.ticketsystem.serviceImpl;

import com.ticketsystem.dto.ProjectRequestDTO;
import com.ticketsystem.dto.ProjectResponseDTO;
import com.ticketsystem.entity.Project;
import com.ticketsystem.entity.Project.ProjectStatus;
import com.ticketsystem.exception.ResourceNotFoundException;
import com.ticketsystem.repository.ProjectRepository;
import com.ticketsystem.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    @Override
    @Transactional
    public ProjectResponseDTO createProject(ProjectRequestDTO requestDTO) {
        log.info("Creating new project: {}", requestDTO.getProjectCode());
        if (projectRepository.existsByProjectCode(requestDTO.getProjectCode())) {
            throw new IllegalArgumentException("Project code already exists: " + requestDTO.getProjectCode());
        }
        Project project = Project.builder()
                .projectCode(requestDTO.getProjectCode())
                .projectName(requestDTO.getProjectName())
                .description(requestDTO.getDescription())
                .status(requestDTO.getStatus())
                .startDate(requestDTO.getStartDate())
                .endDate(requestDTO.getEndDate())
                .build();
        Project saved = projectRepository.save(project);
        log.info("Project created with ID: {}", saved.getId());
        return mapToResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponseDTO> getAllProjects(Pageable pageable) {
        return projectRepository.findAll(pageable).map(this::mapToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponseDTO getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return mapToResponseDTO(project);
    }

    @Override
    @Transactional
    public ProjectResponseDTO updateProject(Long id, ProjectRequestDTO requestDTO) {
        log.info("Updating project with ID: {}", id);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        if (projectRepository.existsByProjectCodeAndIdNot(requestDTO.getProjectCode(), id)) {
            throw new IllegalArgumentException("Project code already exists: " + requestDTO.getProjectCode());
        }
        project.setProjectCode(requestDTO.getProjectCode());
        project.setProjectName(requestDTO.getProjectName());
        project.setDescription(requestDTO.getDescription());
        project.setStatus(requestDTO.getStatus());
        project.setStartDate(requestDTO.getStartDate());
        project.setEndDate(requestDTO.getEndDate());
        Project updated = projectRepository.save(project);
        return mapToResponseDTO(updated);
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        log.info("Deleting project with ID: {}", id);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        projectRepository.delete(project);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponseDTO> searchProjects(String search, ProjectStatus status, Pageable pageable) {
        return projectRepository.searchProjects(search, status, pageable).map(this::mapToResponseDTO);
    }

    private ProjectResponseDTO mapToResponseDTO(Project project) {
        return ProjectResponseDTO.builder()
                .id(project.getId())
                .projectCode(project.getProjectCode())
                .projectName(project.getProjectName())
                .description(project.getDescription())
                .status(project.getStatus())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
