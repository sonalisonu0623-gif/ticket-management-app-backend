package com.ticketsystem.serviceImpl;

import com.ticketsystem.dto.HolidayDTO;
import com.ticketsystem.dto.ShiftHoursDTO;
import com.ticketsystem.dto.SlaConfigDTO;
import com.ticketsystem.entity.Holiday;
import com.ticketsystem.entity.ShiftHours;
import com.ticketsystem.entity.SlaConfig;
import com.ticketsystem.exception.ResourceNotFoundException;
import com.ticketsystem.repository.HolidayRepository;
import com.ticketsystem.repository.ShiftHoursRepository;
import com.ticketsystem.repository.SlaConfigRepository;
import com.ticketsystem.service.ConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConfigurationServiceImpl implements ConfigurationService {

    private final ShiftHoursRepository shiftRepo;
    private final HolidayRepository    holidayRepo;
    private final SlaConfigRepository  slaRepo;

    // ── Shift Hours ──────────────────────────────────────────────────────────

    @Override public List<ShiftHoursDTO> getAllShifts() {
        return shiftRepo.findAll().stream().map(this::toShiftDTO).collect(Collectors.toList());
    }

    @Override @Transactional
    public ShiftHoursDTO createShift(ShiftHoursDTO dto) {
        ShiftHours s = ShiftHours.builder()
                .shiftName(dto.getShiftName())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .isActive(dto.isActive())
                .build();
        return toShiftDTO(shiftRepo.save(s));
    }

    @Override @Transactional
    public ShiftHoursDTO updateShift(Long id, ShiftHoursDTO dto) {
        ShiftHours s = shiftRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found: " + id));
        s.setShiftName(dto.getShiftName());
        s.setStartTime(dto.getStartTime());
        s.setEndTime(dto.getEndTime());
        s.setActive(dto.isActive());
        return toShiftDTO(shiftRepo.save(s));
    }

    @Override @Transactional
    public void deleteShift(Long id) {
        shiftRepo.delete(shiftRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found: " + id)));
    }

    // ── Holidays ─────────────────────────────────────────────────────────────

    @Override public List<HolidayDTO> getAllHolidays() {
        return holidayRepo.findAll().stream().map(this::toHolidayDTO).collect(Collectors.toList());
    }

    @Override @Transactional
    public HolidayDTO createHoliday(HolidayDTO dto) {
        if (holidayRepo.existsByHolidayDate(dto.getHolidayDate()))
            throw new IllegalArgumentException("Holiday already exists on: " + dto.getHolidayDate());
        Holiday h = Holiday.builder()
                .holidayName(dto.getHolidayName())
                .holidayDate(dto.getHolidayDate())
                .description(dto.getDescription())
                .build();
        return toHolidayDTO(holidayRepo.save(h));
    }

    @Override @Transactional
    public HolidayDTO updateHoliday(Long id, HolidayDTO dto) {
        Holiday h = holidayRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday not found: " + id));
        if (holidayRepo.existsByHolidayDateAndIdNot(dto.getHolidayDate(), id))
            throw new IllegalArgumentException("Another holiday exists on: " + dto.getHolidayDate());
        h.setHolidayName(dto.getHolidayName());
        h.setHolidayDate(dto.getHolidayDate());
        h.setDescription(dto.getDescription());
        return toHolidayDTO(holidayRepo.save(h));
    }

    @Override @Transactional
    public void deleteHoliday(Long id) {
        holidayRepo.delete(holidayRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday not found: " + id)));
    }

    // ── SLA Config ───────────────────────────────────────────────────────────

    @Override public List<SlaConfigDTO> getAllSlaConfigs() {
        return slaRepo.findAll().stream().map(this::toSlaDTO).collect(Collectors.toList());
    }

    @Override @Transactional
    public SlaConfigDTO createSlaConfig(SlaConfigDTO dto) {
        if (slaRepo.findByPriorityAndSupportLevel(dto.getPriority(), dto.getSupportLevel()).isPresent())
            throw new IllegalArgumentException("SLA config already exists for " + dto.getPriority() + "/" + dto.getSupportLevel());
        SlaConfig cfg = SlaConfig.builder()
                .priority(dto.getPriority())
                .supportLevel(dto.getSupportLevel())
                .responseTimeHours(dto.getResponseTimeHours())
                .resolutionTimeHours(dto.getResolutionTimeHours())
                .isActive(dto.isActive())
                .build();
        return toSlaDTO(slaRepo.save(cfg));
    }

    @Override @Transactional
    public SlaConfigDTO updateSlaConfig(Long id, SlaConfigDTO dto) {
        SlaConfig cfg = slaRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SLA config not found: " + id));
        cfg.setPriority(dto.getPriority());
        cfg.setSupportLevel(dto.getSupportLevel());
        cfg.setResponseTimeHours(dto.getResponseTimeHours());
        cfg.setResolutionTimeHours(dto.getResolutionTimeHours());
        cfg.setActive(dto.isActive());
        return toSlaDTO(slaRepo.save(cfg));
    }

    @Override @Transactional
    public void deleteSlaConfig(Long id) {
        slaRepo.delete(slaRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SLA config not found: " + id)));
    }

    // ── mappers ──────────────────────────────────────────────────────────────

    private ShiftHoursDTO toShiftDTO(ShiftHours s) {
        ShiftHoursDTO dto = new ShiftHoursDTO();
        dto.setId(s.getId());
        dto.setShiftName(s.getShiftName());
        dto.setStartTime(s.getStartTime());
        dto.setEndTime(s.getEndTime());
        dto.setActive(s.isActive());
        return dto;
    }

    private HolidayDTO toHolidayDTO(Holiday h) {
        HolidayDTO dto = new HolidayDTO();
        dto.setId(h.getId());
        dto.setHolidayName(h.getHolidayName());
        dto.setHolidayDate(h.getHolidayDate());
        dto.setDescription(h.getDescription());
        return dto;
    }

    private SlaConfigDTO toSlaDTO(SlaConfig c) {
        SlaConfigDTO dto = new SlaConfigDTO();
        dto.setId(c.getId());
        dto.setPriority(c.getPriority());
        dto.setSupportLevel(c.getSupportLevel());
        dto.setResponseTimeHours(c.getResponseTimeHours());
        dto.setResolutionTimeHours(c.getResolutionTimeHours());
        dto.setActive(c.isActive());
        return dto;
    }
}
