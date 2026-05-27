package com.ticketsystem.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

/**
 * Calculates elapsed and remaining time using business-hours logic.
 *
 * Example:
 *   Shift: 09:00 – 18:00, Mon–Fri
 *   Ticket raised: Friday 17:00
 *   Resolved:      Monday  11:00
 *   Business hours = Fri(1h) + Mon(2h) = 3h
 */
@Component
public class BusinessHoursCalculator {

    @Value("${app.sla.shift-start-hour:9}")
    private int shiftStart;

    @Value("${app.sla.shift-end-hour:18}")
    private int shiftEnd;

    /**
     * Count business hours (whole hours) between two timestamps.
     * Skips weekends and hours outside the configured shift window.
     */
    public int calculateBusinessHours(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !end.isAfter(start)) return 0;

        int hours = 0;
        LocalDateTime current = start;

        while (current.isBefore(end)) {
            if (isBusinessHour(current)) {
                hours++;
            }
            current = current.plusHours(1);
        }
        return hours;
    }

    /**
     * Returns true if the given datetime falls inside a business hour:
     *  – Mon–Fri
     *  – shiftStart (inclusive) to shiftEnd (exclusive)
     */
    public boolean isBusinessHour(LocalDateTime dt) {
        DayOfWeek dow = dt.getDayOfWeek();
        boolean weekday = dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
        int hour = dt.getHour();
        return weekday && hour >= shiftStart && hour < shiftEnd;
    }

    /**
     * Advance a datetime by the given number of business hours.
     * Used for computing SLA deadline from ticket creation time.
     */
    public LocalDateTime addBusinessHours(LocalDateTime from, int businessHours) {
        if (from == null || businessHours <= 0) return from;

        LocalDateTime current = from;
        int remaining = businessHours;

        while (remaining > 0) {
            current = current.plusHours(1);
            if (isBusinessHour(current)) {
                remaining--;
            }
        }
        return current;
    }

    /**
     * Human-readable duration string, e.g. "3h 20m"
     */
    public String formatDuration(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return null;
        long totalMinutes = java.time.Duration.between(start, end).toMinutes();
        if (totalMinutes < 0) return "N/A";
        long hours   = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return hours + "h " + minutes + "m";
    }

    public int getShiftStart() { return shiftStart; }
    public int getShiftEnd()   { return shiftEnd; }
}