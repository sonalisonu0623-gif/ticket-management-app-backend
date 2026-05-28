package com.ticketsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "employees")
@ToString(exclude = "employees")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_name", nullable = false, unique = true, length = 150)
    private String projectName;

    @Column(name = "project_code", unique = true, length = 30)
    private String projectCode;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "support_email", length = 150)
    private String supportEmail;

    /** Default SLA window in business hours (e.g. 24 = 24 business hours) */
    @Column(name = "sla_hours")
    @Builder.Default
    private Integer slaHours = 24;

    @Column(name = "shift_timing", length = 50)
    private String shiftTiming;

    /** ACTIVE | INACTIVE */
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    @ManyToMany(mappedBy = "projects", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Employee> employees = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
