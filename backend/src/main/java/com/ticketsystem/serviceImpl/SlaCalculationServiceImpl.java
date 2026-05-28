package com.ticketsystem.serviceImpl;

import com.ticketsystem.entity.Employee;
import com.ticketsystem.entity.Holiday;
import com.ticketsystem.entity.ShiftHours;
import com.ticketsystem.entity.SlaConfig;
import com.ticketsystem.repository.EmployeeRepository;
import com.ticketsystem.repository.HolidayRepository;
import com.ticketsystem.repository.ShiftHoursRepository;
import com.ticketsystem.repository.SlaConfigRepository;
import com.ticketsystem.service.SlaCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Business-hour SLA calculation.
 *
 * Rules (in priority order):
 *  1. Saturdays and Sundays are always non-working.
 *  2. Any date in the holidays table is non-working.
 *  3. On working days, only minutes that fall within at least one active
 *     ShiftHours window are counted.
 *  4. If an assignedEmployee is supplied the calculation uses that employee's
 *     specific shift (if set); otherwise falls back to all active shifts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SlaCalculationServiceImpl implements SlaCalculationService {

    private final ShiftHoursRepository shiftHoursRepository;
    private final HolidayRepository    holidayRepository;
    private final SlaConfigRepository  slaConfigRepository;
    private final EmployeeRepository   employeeRepository;

    // ── Public API ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public long workingMinutesBetween(LocalDateTime start, LocalDateTime end) {
        return workingMinutesBetween(start, end, null);
    }

    @Override
    @Transactional(readOnly = true)
    public long workingMinutesBetween(LocalDateTime start, LocalDateTime end,
                                      String assignedEmployeeUsername) {
        if (!start.isBefore(end)) return 0;

        List<ShiftHours> shifts = resolveShifts(assignedEmployeeUsername);
        Set<LocalDate> holidays = fetchHolidayDates(start.toLocalDate(), end.toLocalDate());

        if (shifts.isEmpty()) {
            // No shift configuration at all — count all non-weekend, non-holiday minutes
            return countWorkingMinutes(start, end, holidays);
        }

        // Sort shifts by start time to process them in order within each day
        List<ShiftHours> sortedShifts = shifts.stream()
                .sorted(Comparator.comparing(ShiftHours::getStartTime))
                .collect(Collectors.toList());

        long totalMinutes = 0;
        LocalDateTime cursor = start;

        while (cursor.isBefore(end)) {
            LocalDate day = cursor.toLocalDate();

            if (isNonWorkingDay(day, holidays)) {
                cursor = day.plusDays(1).atStartOfDay();
                continue;
            }

            for (ShiftHours shift : sortedShifts) {
                LocalDateTime shiftStart = day.atTime(shift.getStartTime());
                LocalDateTime shiftEnd   = day.atTime(shift.getEndTime());

                // Handle overnight shifts (e.g. 22:00 – 06:00)
                if (shift.getEndTime().isBefore(shift.getStartTime())) {
                    shiftEnd = day.plusDays(1).atTime(shift.getEndTime());
                }

                LocalDateTime overlapStart = cursor.isAfter(shiftStart) ? cursor : shiftStart;
                LocalDateTime overlapEnd   = end.isBefore(shiftEnd) ? end : shiftEnd;

                if (overlapStart.isBefore(overlapEnd)) {
                    totalMinutes += ChronoUnit.MINUTES.between(overlapStart, overlapEnd);
                }
            }

            cursor = day.plusDays(1).atStartOfDay();
        }

        return totalMinutes;
    }

    @Override
    @Transactional(readOnly = true)
    public LocalDateTime addWorkingHours(LocalDateTime start, double workingHours) {
        return addWorkingHours(start, workingHours, null);
    }

    @Override
    @Transactional(readOnly = true)
    public LocalDateTime addWorkingHours(LocalDateTime start, double workingHours,
                                         String assignedEmployeeUsername) {
        long remainingMinutes = Math.round(workingHours * 60);

        List<ShiftHours> shifts = resolveShifts(assignedEmployeeUsername);

        if (shifts.isEmpty()) {
            return addRawWorkingMinutes(start, remainingMinutes);
        }

        List<ShiftHours> sortedShifts = shifts.stream()
                .sorted(Comparator.comparing(ShiftHours::getStartTime))
                .collect(Collectors.toList());

        LocalDate endHorizon = start.toLocalDate().plusYears(1);
        Set<LocalDate> holidays = fetchHolidayDates(start.toLocalDate(), endHorizon);

        LocalDateTime cursor = start;

        while (remainingMinutes > 0) {
            LocalDate day = cursor.toLocalDate();

            if (isNonWorkingDay(day, holidays)) {
                // Jump to start of first shift on next day
                cursor = day.plusDays(1).atTime(sortedShifts.get(0).getStartTime());
                continue;
            }

            boolean advancedThisDay = false;

            for (ShiftHours shift : sortedShifts) {
                if (remainingMinutes <= 0) break;

                LocalDateTime shiftStart = day.atTime(shift.getStartTime());
                LocalDateTime shiftEnd   = day.atTime(shift.getEndTime());

                // Handle overnight shifts
                if (shift.getEndTime().isBefore(shift.getStartTime())) {
                    shiftEnd = day.plusDays(1).atTime(shift.getEndTime());
                }

                // Cursor is before this shift window — advance to shift start
                if (cursor.isBefore(shiftStart)) cursor = shiftStart;

                // Cursor is past this shift window — skip
                if (!cursor.isBefore(shiftEnd)) continue;

                long available = ChronoUnit.MINUTES.between(cursor, shiftEnd);
                if (available >= remainingMinutes) {
                    return cursor.plusMinutes(remainingMinutes);
                }

                remainingMinutes -= available;
                cursor = shiftEnd;
                advancedThisDay = true;
            }

            // Move to first shift of next working day
            cursor = day.plusDays(1).atTime(sortedShifts.get(0).getStartTime());
        }

        return cursor;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSlaBreached(String priority, String supportLevel, LocalDateTime createdAt) {
        return isSlaBreached(priority, supportLevel, createdAt, null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSlaBreached(String priority, String supportLevel,
                                  LocalDateTime createdAt, String assignedEmployee) {
        Optional<SlaConfig> cfg = slaConfigRepository
                .findByPriorityAndSupportLevel(priority, supportLevel);
        if (cfg.isEmpty()) return false;

        LocalDateTime deadline = addWorkingHours(createdAt, cfg.get().getResolutionTimeHours(),
                                                  assignedEmployee);
        return LocalDateTime.now().isAfter(deadline);
    }

    // ── Shift resolution ──────────────────────────────────────────────────────

    /**
     * Returns the shifts to use for a given employee username.
     * If the employee has a specific shift assigned, use only that shift.
     * Otherwise, use all active shifts system-wide.
     */
    private List<ShiftHours> resolveShifts(String assignedEmployeeUsername) {
        if (assignedEmployeeUsername != null && !assignedEmployeeUsername.isBlank()) {
            Optional<Employee> emp = employeeRepository.findByUsername(assignedEmployeeUsername);
            // Also try matching by employee name (tickets store employeeName not username)
            if (emp.isEmpty()) {
                emp = employeeRepository.findByEmployeeName(assignedEmployeeUsername);
            }
            if (emp.isPresent() && emp.get().getShiftHours() != null) {
                return List.of(emp.get().getShiftHours());
            }
        }
        return shiftHoursRepository.findByIsActiveTrue();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean isNonWorkingDay(LocalDate day, Set<LocalDate> holidays) {
        DayOfWeek dow = day.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY
               || holidays.contains(day);
    }

    private Set<LocalDate> fetchHolidayDates(LocalDate from, LocalDate to) {
        return holidayRepository.findByHolidayDateBetween(from, to)
                .stream()
                .map(Holiday::getHolidayDate)
                .collect(Collectors.toSet());
    }

    /** Count non-weekend, non-holiday minutes without any shift constraint. */
    private long countWorkingMinutes(LocalDateTime start, LocalDateTime end,
                                     Set<LocalDate> holidays) {
        long total = 0;
        LocalDate day = start.toLocalDate();
        LocalDate endDay = end.toLocalDate();

        while (!day.isAfter(endDay)) {
            if (!isNonWorkingDay(day, holidays)) {
                LocalDateTime dayStart = day.equals(start.toLocalDate())
                        ? start : day.atStartOfDay();
                LocalDateTime dayEnd = day.equals(endDay)
                        ? end : day.atTime(23, 59, 59);
                total += ChronoUnit.MINUTES.between(dayStart, dayEnd);
            }
            day = day.plusDays(1);
        }
        return total;
    }

    private LocalDateTime addRawWorkingMinutes(LocalDateTime start, long minutes) {
        LocalDate endHorizon = start.toLocalDate().plusYears(1);
        Set<LocalDate> holidays = fetchHolidayDates(start.toLocalDate(), endHorizon);
        LocalDateTime cursor = start;
        long added = 0;
        while (added < minutes) {
            cursor = cursor.plusMinutes(1);
            if (!isNonWorkingDay(cursor.toLocalDate(), holidays)) added++;
        }
        return cursor;
    }
}
