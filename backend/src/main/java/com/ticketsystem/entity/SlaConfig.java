package com.ticketsystem.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sla_configs",
       uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "priority_level"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /** P1 - Critical | P2 - High | P3 - Medium | P4 - Low */
    @Column(name = "priority_level", nullable = false, length = 30)
    private String priorityLevel;

    /** Response SLA in business hours */
    @Column(name = "response_time_sla", nullable = false)
    @Builder.Default
    private Integer responseTimeSla = 4;

    /** Resolution SLA in business hours */
    @Column(name = "resolution_time_sla", nullable = false)
    @Builder.Default
    private Integer resolutionTimeSla = 24;

    /** Escalation threshold in business hours */
    @Column(name = "escalation_time_sla")
    @Builder.Default
    private Integer escalationTimeSla = 8;
}
