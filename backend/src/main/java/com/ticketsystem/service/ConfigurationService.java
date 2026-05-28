package com.ticketsystem.service;

import com.ticketsystem.dto.HolidayDTO;
import com.ticketsystem.dto.ShiftHoursDTO;
import com.ticketsystem.dto.SlaConfigDTO;
import java.util.List;

public interface ConfigurationService {
    // ── Shift Hours ──────────────────────────────────────────────────────
    List<ShiftHoursDTO> getAllShifts();
    ShiftHoursDTO createShift(ShiftHoursDTO dto);
    ShiftHoursDTO updateShift(Long id, ShiftHoursDTO dto);
    void deleteShift(Long id);

    // ── Holidays ─────────────────────────────────────────────────────────
    List<HolidayDTO> getAllHolidays();
    HolidayDTO createHoliday(HolidayDTO dto);
    HolidayDTO updateHoliday(Long id, HolidayDTO dto);
    void deleteHoliday(Long id);

    // ── SLA Config ───────────────────────────────────────────────────────
    List<SlaConfigDTO> getAllSlaConfigs();
    SlaConfigDTO createSlaConfig(SlaConfigDTO dto);
    SlaConfigDTO updateSlaConfig(Long id, SlaConfigDTO dto);
    void deleteSlaConfig(Long id);
}
