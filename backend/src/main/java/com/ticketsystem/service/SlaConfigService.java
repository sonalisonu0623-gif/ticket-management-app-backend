package com.ticketsystem.service;

import com.ticketsystem.dto.SlaConfigDTO;
import com.ticketsystem.entity.Project;
import com.ticketsystem.entity.SlaConfig;
import com.ticketsystem.exception.DuplicateResourceException;
import com.ticketsystem.exception.ResourceNotFoundException;
import com.ticketsystem.repository.ProjectRepository;
import com.ticketsystem.repository.SlaConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SlaConfigService {

    private final SlaConfigRepository slaConfigRepository;
    private final ProjectRepository   projectRepository;

    public List<SlaConfigDTO> getAll() {
        return slaConfigRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<SlaConfigDTO> getByProject(Long projectId) {
        return slaConfigRepository.findByProjectId(projectId).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    public SlaConfigDTO getById(Long id) {
        return toDTO(findById(id));
    }

    @Transactional
    public SlaConfigDTO create(SlaConfigDTO dto) {
        if (slaConfigRepository.existsByProjectIdAndPriorityLevel(
                dto.getProjectId(), dto.getPriorityLevel())) {
            throw new DuplicateResourceException(
                "SLA config already exists for project " + dto.getProjectId()
                + " and priority " + dto.getPriorityLevel());
        }
        Project project = findProject(dto.getProjectId());
        SlaConfig config = SlaConfig.builder()
                .project(project)
                .priorityLevel(dto.getPriorityLevel())
                .responseTimeSla(dto.getResponseTimeSla() != null ? dto.getResponseTimeSla() : 4)
                .resolutionTimeSla(dto.getResolutionTimeSla() != null ? dto.getResolutionTimeSla() : 24)
                .escalationTimeSla(dto.getEscalationTimeSla() != null ? dto.getEscalationTimeSla() : 8)
                .build();
        return toDTO(slaConfigRepository.save(config));
    }

    @Transactional
    public SlaConfigDTO update(Long id, SlaConfigDTO dto) {
        SlaConfig config = findById(id);
        if (dto.getResponseTimeSla()  != null) config.setResponseTimeSla(dto.getResponseTimeSla());
        if (dto.getResolutionTimeSla()!= null) config.setResolutionTimeSla(dto.getResolutionTimeSla());
        if (dto.getEscalationTimeSla()!= null) config.setEscalationTimeSla(dto.getEscalationTimeSla());
        return toDTO(slaConfigRepository.save(config));
    }

    @Transactional
    public void delete(Long id) {
        slaConfigRepository.delete(findById(id));
    }

    private SlaConfig findById(Long id) {
        return slaConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SLA config not found: " + id));
    }

    private Project findProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
    }

    private SlaConfigDTO toDTO(SlaConfig s) {
        return SlaConfigDTO.builder()
                .id(s.getId())
                .projectId(s.getProject().getId())
                .projectName(s.getProject().getProjectName())
                .priorityLevel(s.getPriorityLevel())
                .responseTimeSla(s.getResponseTimeSla())
                .resolutionTimeSla(s.getResolutionTimeSla())
                .escalationTimeSla(s.getEscalationTimeSla())
                .build();
    }
}
