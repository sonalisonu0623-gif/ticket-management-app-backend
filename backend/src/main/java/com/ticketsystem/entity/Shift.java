package com.ticketsystem.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shifts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shift_name", nullable = false, unique = true, length = 100)
    private String shiftName;

    /** e.g. "09:00" */
    @Column(name = "start_time", nullable = false, length = 10)
    private String startTime;

    /** e.g. "18:00" */
    @Column(name = "end_time", nullable = false, length = 10)
    private String endTime;

    /**
     * Comma-separated working days: "Monday,Tuesday,Wednesday,Thursday,Friday"
     */
    @Column(name = "working_days", length = 200)
    private String workingDays;

    @Column(name = "timezone", length = 60)
    @Builder.Default
    private String timezone = "Asia/Kolkata";
}
