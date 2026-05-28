package com.ticketsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "projects")
@ToString(exclude = "projects")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", unique = true, length = 30)
    private String employeeId;

    @Column(name = "employee_name", nullable = false, length = 150)
    private String employeeName;

    @Column(name = "email", unique = true, length = 150)
    private String email;

    /** L1 | L2 | L3 */
    @Column(name = "support_level", length = 10)
    private String supportLevel;

    /** ADMIN | PROJECT_MANAGER | L1_SUPPORT | L2_SUPPORT | L3_SUPPORT */
    @Column(name = "role", length = 30)
    @Builder.Default
    private String role = "L1_SUPPORT";

    @Column(name = "designation", length = 100)
    private String designation;

    @Column(name = "shift", length = 50)
    private String shift;

    /** ACTIVE | INACTIVE */
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    /**
     * Many employees belong to many projects.
     * Owner side of the join table.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "employee_projects",
        joinColumns = @JoinColumn(name = "employee_id"),
        inverseJoinColumns = @JoinColumn(name = "project_id")
    )
    @Builder.Default
    private Set<Project> projects = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
