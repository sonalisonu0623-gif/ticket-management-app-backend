package com.ticketsystem.config;

import com.ticketsystem.entity.*;
import com.ticketsystem.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TicketRepository ticketRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Data already initialized, skipping...");
            return;
        }

        log.info("Initializing sample data...");

        // Create admin user
        User admin = userRepository.save(User.builder()
                .username("admin").email("admin@company.com")
                .password(passwordEncoder.encode("admin123"))
                .fullName("System Administrator").department("IT")
                .role(Role.ADMIN).isActive(true).build());

        // Create support engineers
        User eng1 = userRepository.save(User.builder()
                .username("john.doe").email("john.doe@company.com")
                .password(passwordEncoder.encode("password123"))
                .fullName("John Doe").department("IT Support")
                .role(Role.SUPPORT_ENGINEER).isActive(true).build());

        User eng2 = userRepository.save(User.builder()
                .username("jane.smith").email("jane.smith@company.com")
                .password(passwordEncoder.encode("password123"))
                .fullName("Jane Smith").department("IT Support")
                .role(Role.SUPPORT_ENGINEER).isActive(true).build());

        User eng3 = userRepository.save(User.builder()
                .username("mike.johnson").email("mike.johnson@company.com")
                .password(passwordEncoder.encode("password123"))
                .fullName("Mike Johnson").department("IT Support")
                .role(Role.SUPPORT_ENGINEER).isActive(true).build());

        // Create employees
        User emp1 = userRepository.save(User.builder()
                .username("alice.brown").email("alice.brown@company.com")
                .password(passwordEncoder.encode("password123"))
                .fullName("Alice Brown").department("HR")
                .role(Role.EMPLOYEE).isActive(true).build());

        User emp2 = userRepository.save(User.builder()
                .username("bob.wilson").email("bob.wilson@company.com")
                .password(passwordEncoder.encode("password123"))
                .fullName("Bob Wilson").department("Finance")
                .role(Role.EMPLOYEE).isActive(true).build());

        // Create projects
        Project hrPortal = projectRepository.save(Project.builder()
                .name("HR Portal").description("Human Resources Management System")
                .projectCode("HRP").isActive(true).build());

        Project erpSystem = projectRepository.save(Project.builder()
                .name("ERP System").description("Enterprise Resource Planning")
                .projectCode("ERP").isActive(true).build());

        Project telemedicine = projectRepository.save(Project.builder()
                .name("Telemedicine").description("Telemedicine Platform")
                .projectCode("TLM").isActive(true).build());

        Project inventory = projectRepository.save(Project.builder()
                .name("Inventory Management").description("Warehouse and Inventory System")
                .projectCode("INV").isActive(true).build());

        Project payroll = projectRepository.save(Project.builder()
                .name("Payroll System").description("Payroll Processing System")
                .projectCode("PAY").isActive(true).build());

        // Create sample tickets
        LocalDateTime now = LocalDateTime.now();
        createTicket("INC-9001", hrPortal, emp1, eng1, Priority.P1_CRITICAL, TicketStatus.OPEN,
                SupportLevel.L1, "Unable to login to HR portal. Getting 403 error on the login screen.", now.minusDays(2), admin);
        createTicket("INC-9002", erpSystem, emp2, eng2, Priority.P2_HIGH, TicketStatus.IN_PROGRESS,
                SupportLevel.L2, "ERP module crashing when generating quarterly reports.", now.minusDays(1), admin);
        createTicket("INC-9003", telemedicine, emp1, eng3, Priority.P3_MEDIUM, TicketStatus.RESOLVED,
                SupportLevel.L1, "Video call feature not working on Safari browser.", now.minusDays(5), emp1);
        createTicket("INC-9004", inventory, emp2, null, Priority.P4_LOW, TicketStatus.OPEN,
                SupportLevel.L1, "Barcode scanner driver needs update.", now.minusHours(3), emp2);
        createTicket("INC-9005", payroll, emp1, eng1, Priority.P2_HIGH, TicketStatus.ON_HOLD,
                SupportLevel.L3, "Payroll calculation incorrect for employees with overtime.", now.minusDays(3), admin);
        createTicket("INC-9006", hrPortal, emp2, eng2, Priority.P1_CRITICAL, TicketStatus.CLOSED,
                SupportLevel.L2, "Database connection pool exhausted causing downtime.", now.minusDays(7), admin);
        createTicket("INC-9007", erpSystem, emp1, eng3, Priority.P3_MEDIUM, TicketStatus.IN_PROGRESS,
                SupportLevel.L2, "PDF export feature generating blank pages intermittently.", now.minusHours(8), emp1);
        createTicket("INC-9008", telemedicine, emp2, eng1, Priority.P2_HIGH, TicketStatus.REOPENED,
                SupportLevel.L2, "Appointment booking not sending email confirmations.", now.minusDays(4), emp2);

        log.info("Sample data initialized. Admin: admin/admin123, Support: john.doe/password123");
    }

    private void createTicket(String ticketNumber, Project project, User createdBy, User assignedTo,
                               Priority priority, TicketStatus status, SupportLevel level,
                               String description, LocalDateTime createdAt, User updatedBy) {
        LocalDateTime sla = switch (priority) {
            case P1_CRITICAL -> createdAt.plusHours(4);
            case P2_HIGH -> createdAt.plusHours(8);
            case P3_MEDIUM -> createdAt.plusHours(24);
            case P4_LOW -> createdAt.plusHours(72);
        };

        Ticket ticket = Ticket.builder()
                .ticketNumber(ticketNumber).issueDescription(description)
                .project(project).createdBy(createdBy).assignedTo(assignedTo)
                .priority(priority).currentStatus(status).supportLevel(level)
                .generationDateTime(createdAt).slaDueDateTime(sla)
                .slaBreached(LocalDateTime.now().isAfter(sla) && status != TicketStatus.RESOLVED && status != TicketStatus.CLOSED)
                .build();

        if (status == TicketStatus.RESOLVED || status == TicketStatus.CLOSED) {
            ticket.setResolutionDateTime(createdAt.plusHours(6));
            ticket.setResolutionTimeMinutes(360L);
            ticket.setResolutionDetails("Issue has been investigated and resolved. Root cause identified and fix applied.");
        }
        ticketRepository.save(ticket);
    }
}
