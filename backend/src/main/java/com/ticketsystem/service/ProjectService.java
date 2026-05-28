package com.ticketsystem.service;

import com.ticketsystem.dto.ProjectRequestDTO;
import com.ticketsystem.dto.ProjectResponseDTO;
import com.ticketsystem.entity.Project.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectService {

    ProjectResponseDTO createProject(ProjectRequestDTO requestDTO);

    Page<ProjectResponseDTO> getAllProjects(Pageable pageable);

    ProjectResponseDTO getProjectById(Long id);

    ProjectResponseDTO updateProject(Long id, ProjectRequestDTO requestDTO);

    void deleteProject(Long id);

    Page<ProjectResponseDTO> searchProjects(String search, ProjectStatus status, Pageable pageable);
}
