package com.ticketsystem.service;

import com.ticketsystem.dto.ShiftDTO;
import com.ticketsystem.entity.Shift;
import com.ticketsystem.exception.DuplicateResourceException;
import com.ticketsystem.exception.ResourceNotFoundException;
import com.ticketsystem.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShiftService {

    private final ShiftRepository shiftRepository;

    public List<ShiftDTO> getAll() {
        return shiftRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ShiftDTO getById(Long id) {
        return toDTO(findById(id));
    }

    @Transactional
    public ShiftDTO create(ShiftDTO dto) {
        if (shiftRepository.existsByShiftName(dto.getShiftName().trim())) {
            throw new DuplicateResourceException("Shift name already exists: " + dto.getShiftName());
        }
        return toDTO(shiftRepository.save(toEntity(dto)));
    }

    @Transactional
    public ShiftDTO update(Long id, ShiftDTO dto) {
        Shift shift = findById(id);
        if (!shift.getShiftName().equalsIgnoreCase(dto.getShiftName().trim())
                && shiftRepository.existsByShiftName(dto.getShiftName().trim())) {
            throw new DuplicateResourceException("Shift name already taken: " + dto.getShiftName());
        }
        shift.setShiftName(dto.getShiftName().trim());
        shift.setStartTime(dto.getStartTime());
        shift.setEndTime(dto.getEndTime());
        shift.setWorkingDays(dto.getWorkingDays() != null
                ? String.join(",", dto.getWorkingDays()) : null);
        shift.setTimezone(dto.getTimezone());
        return toDTO(shiftRepository.save(shift));
    }

    @Transactional
    public void delete(Long id) {
        shiftRepository.delete(findById(id));
    }

    private Shift findById(Long id) {
        return shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found: " + id));
    }

    private Shift toEntity(ShiftDTO dto) {
        return Shift.builder()
                .shiftName(dto.getShiftName().trim())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .workingDays(dto.getWorkingDays() != null
                        ? String.join(",", dto.getWorkingDays()) : null)
                .timezone(dto.getTimezone() != null ? dto.getTimezone() : "Asia/Kolkata")
                .build();
    }

    private ShiftDTO toDTO(Shift s) {
        List<String> days = (s.getWorkingDays() != null && !s.getWorkingDays().isBlank())
                ? Arrays.asList(s.getWorkingDays().split(","))
                : List.of();
        return ShiftDTO.builder()
                .id(s.getId())
                .shiftName(s.getShiftName())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .workingDays(days)
                .timezone(s.getTimezone())
                .build();
    }
}
