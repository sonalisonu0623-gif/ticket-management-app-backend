package com.ticketsystem.service;

import java.time.LocalDateTime;

public interface SlaCalculationService {

    /**
     * Count working minutes between two timestamps using system-wide shift config.
     */
    long workingMinutesBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Count working minutes between two timestamps respecting the assigned
     * employee's shift (falls back to all active shifts if employee has none).
     */
    long workingMinutesBetween(LocalDateTime start, LocalDateTime end,
                                String assignedEmployeeUsername);

    /**
     * Add working hours to a start time using system-wide shift config.
     */
    LocalDateTime addWorkingHours(LocalDateTime start, double workingHours);

    /**
     * Add working hours to a start time respecting the assigned employee's shift.
     */
    LocalDateTime addWorkingHours(LocalDateTime start, double workingHours,
                                   String assignedEmployeeUsername);

    /**
     * Check if SLA is breached for a ticket using system-wide shifts.
     */
    boolean isSlaBreached(String priority, String supportLevel, LocalDateTime createdAt);

    /**
     * Check if SLA is breached taking into account the assigned employee's shift.
     */
    boolean isSlaBreached(String priority, String supportLevel,
                           LocalDateTime createdAt, String assignedEmployee);
}
