package com.ticketsystem.config;

import com.ticketsystem.entity.*;
import com.ticketsystem.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ProjectRepository  projectRepository;
    private final EmployeeRepository employeeRepository;
    private final TicketRepository   ticketRepository;
    private final UserRepository     userRepository;
    private final ShiftRepository    shiftRepository;
    private final SlaConfigRepository slaConfigRepository;
    private final PasswordEncoder    passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (projectRepository.count() > 0) {
            log.info("Seed data already present — skipping initialisation.");
            return;
        }

        log.info("Initialising TicketOps enterprise seed data...");

        seedUsers();
        List<Project>  projects  = seedProjects();
        List<Employee> employees = seedEmployees(projects);
        seedSlaConfigs(projects);
        seedTickets(projects, employees);

        log.info("✓ Seed complete: {} projects, {} employees, {} tickets, {} users",
                projectRepository.count(),
                employeeRepository.count(),
                ticketRepository.count(),
                userRepository.count());
    }

    // ── Users ────────────────────────────────────────────────
    private void seedUsers() {
        // password = "password" for all seed accounts
        String encoded = passwordEncoder.encode("password");

        userRepository.saveAll(List.of(
            User.builder().username("admin")     .email("admin@company.com")     .password(encoded).role("ADMIN")          .isActive(true).build(),
            User.builder().username("pm_marcus") .email("pm.marcus@company.com") .password(encoded).role("PROJECT_MANAGER").isActive(true).build(),
            User.builder().username("l1_john")   .email("l1.john@company.com")   .password(encoded).role("L1_SUPPORT")     .isActive(true).build(),
            User.builder().username("l2_sarah")  .email("l2.sarah@company.com")  .password(encoded).role("L2_SUPPORT")     .isActive(true).build(),
            User.builder().username("l3_david")  .email("l3.david@company.com")  .password(encoded).role("L3_SUPPORT")     .isActive(true).build(),
            User.builder().username("user_test") .email("user.test@company.com") .password(encoded).role("USER")           .isActive(true).build()
        ));
        log.info("  → Users seeded");
    }

    // ── Projects ─────────────────────────────────────────────
    private List<Project> seedProjects() {
        List<Project> saved = projectRepository.saveAll(List.of(
            project("HR Portal",       "HRP", "Human Resources management and employee self-service portal.",        "support-hr@company.com",  24, "ACTIVE"),
            project("ERP System",      "ERP", "Enterprise Resource Planning — finance, procurement, reporting.",    "support-erp@company.com", 16, "ACTIVE"),
            project("Telemedicine",    "TLM", "Remote patient consultation and healthcare records platform.",        "support-tlm@company.com",  8, "ACTIVE"),
            project("Payroll System",  "PAY", "Payroll processing, salary slips, and statutory compliance.",         "support-pay@company.com", 12, "ACTIVE"),
            project("Inventory Mgmt",  "INV", "Warehouse stock tracking and inventory reconciliation.",              "support-inv@company.com", 24, "ACTIVE"),
            project("Customer Portal", "CUS", "B2C customer self-service and order tracking platform.",              "support-cus@company.com", 48, "ACTIVE"),
            project("DevOps Pipeline", "DEV", "CI/CD pipeline, infrastructure monitoring, deployment support.",      "support-dev@company.com",  4, "ACTIVE")
        ));
        log.info("  → {} projects seeded", saved.size());
        return saved;
    }

    private Project project(String name, String code, String desc, String email, int sla, String status) {
        return Project.builder()
                .projectName(name).projectCode(code).description(desc)
                .supportEmail(email).slaHours(sla).shiftTiming("09:00-18:00").status(status)
                .build();
    }

    // ── Employees ────────────────────────────────────────────
    private List<Employee> seedEmployees(List<Project> p) {
        Project hrp = p.get(0), erp = p.get(1), tlm = p.get(2),
                pay = p.get(3), inv = p.get(4), cus = p.get(5), dev = p.get(6);

        List<Employee> employees = employeeRepository.saveAll(List.of(
            emp("EMP-0001","John Davis",    "john.davis@company.com",   "L1","L1_SUPPORT",    "Junior Support Analyst",   Set.of(hrp, pay, inv)),
            emp("EMP-0002","Sarah Kim",     "sarah.kim@company.com",    "L2","L2_SUPPORT",    "Senior Support Engineer",  Set.of(hrp, erp, tlm, pay)),
            emp("EMP-0003","David Raj",     "david.raj@company.com",    "L3","L3_SUPPORT",    "Principal Engineer",       Set.of(hrp, erp, tlm, dev)),
            emp("EMP-0004","Maria Torres",  "maria.torres@company.com", "L1","L1_SUPPORT",    "Support Analyst",          Set.of(hrp, inv, cus)),
            emp("EMP-0005","Alex Park",     "alex.park@company.com",    "L2","L2_SUPPORT",    "Support Engineer",         Set.of(erp, pay, cus)),
            emp("EMP-0006","Chen Wei",      "chen.wei@company.com",     "L3","L3_SUPPORT",    "Systems Architect",        Set.of(tlm, dev)),
            emp("EMP-0007","Priya Sharma",  "priya.sharma@company.com", "L1","L1_SUPPORT",    "Help Desk Specialist",     Set.of(inv, cus)),
            emp("EMP-0008","Marcus Johnson","marcus.j@company.com",     "L2","PROJECT_MANAGER","Project Manager",         Set.of(erp)),
            emp("EMP-0009","Fatima Al-Said","fatima.as@company.com",    "L2","L2_SUPPORT",    "Application Support Lead", Set.of(erp, tlm, pay, cus)),
            emp("EMP-0010","Carlos Mendes", "carlos.m@company.com",     "L3","L3_SUPPORT",    "Database Engineer",        Set.of(dev))
        ));
        log.info("  → {} employees seeded", employees.size());
        return employees;
    }

    private Employee emp(String eid, String name, String email, String level,
                         String role, String desg, Set<Project> projects) {
        return Employee.builder()
                .employeeId(eid).employeeName(name).email(email)
                .supportLevel(level).role(role).designation(desg)
                .shift("Morning Shift").status("ACTIVE").projects(projects)
                .build();
    }

    // ── SLA Configs ──────────────────────────────────────────
    private void seedSlaConfigs(List<Project> projects) {
        Project hrp = projects.get(0), erp = projects.get(1), tlm = projects.get(2);

        slaConfigRepository.saveAll(List.of(
            // HR Portal
            sla(hrp, "P1 - Critical", 1,  8,  2),
            sla(hrp, "P2 - High",     2,  16, 4),
            sla(hrp, "P3 - Medium",   4,  24, 8),
            sla(hrp, "P4 - Low",      8,  48, 16),
            // ERP (tighter)
            sla(erp, "P1 - Critical", 1,  4,  1),
            sla(erp, "P2 - High",     2,  8,  3),
            sla(erp, "P3 - Medium",   4,  16, 6),
            sla(erp, "P4 - Low",      8,  32, 12),
            // Telemedicine (tightest — patient safety)
            sla(tlm, "P1 - Critical", 0,  2,  1),
            sla(tlm, "P2 - High",     1,  6,  2),
            sla(tlm, "P3 - Medium",   2,  12, 4),
            sla(tlm, "P4 - Low",      4,  24, 8)
        ));
        log.info("  → SLA configs seeded");
    }

    private SlaConfig sla(Project p, String priority, int resp, int resol, int esc) {
        return SlaConfig.builder()
                .project(p).priorityLevel(priority)
                .responseTimeSla(resp).resolutionTimeSla(resol).escalationTimeSla(esc)
                .build();
    }

    // ── Tickets ──────────────────────────────────────────────
    private void seedTickets(List<Project> projects, List<Employee> employees) {
        LocalDateTime now = LocalDateTime.now();

        Project hrp = projects.get(0), erp = projects.get(1), tlm = projects.get(2),
                pay = projects.get(3), inv = projects.get(4), cus = projects.get(5), dev = projects.get(6);

        Employee e1 = employees.get(0), e2 = employees.get(1), e3 = employees.get(2),
                 e4 = employees.get(3), e5 = employees.get(4), e6 = employees.get(5),
                 e7 = employees.get(6), e9 = employees.get(8), e10 = employees.get(9);

        ticketRepository.saveAll(List.of(
            ticket("INC-1001", hrp, "Unable to login to HR portal. Password reset not working for multiple users in Finance.", e1, "L1", "P2 - High", now.minusDays(3), now.minusDays(2), "24h 0m", 24, "Resolved", "Password reset policy reconfigured and accounts unlocked.", "Affected 15 users", false),
            ticket("INC-1002", hrp, "Employee self-service portal showing incorrect leave balance for all employees hired after Jan 2024.", e2, "L2", "P2 - High", now.minusDays(5), null, null, 72, "In Progress", null, "Leave accrual calculation bug", true),
            ticket("INC-1003", hrp, "Performance appraisal form submission failing with 500 error. Deadline in 2 days.", e3, "L3", "P1 - Critical", now.minusDays(1), now.minusHours(6), "18h 0m", 18, "Resolved", "Database constraint violation fixed.", "Appraisal cycle at risk", false),
            ticket("INC-1004", erp, "ERP month-end report crashing for data > 10,000 rows. Finance team blocked for closing.", e3, "L3", "P1 - Critical", now.minusDays(2), null, null, 48, "In Progress", null, "Critical month-end closing", true),
            ticket("INC-1005", erp, "Purchase order approval workflow emails not delivered. 47 POs stuck in pending state.", e5, "L2", "P2 - High", now.minusHours(12), null, null, 12, "Open", null, "SMTP relay issue suspected", false),
            ticket("INC-1006", erp, "User access provisioning taking 24+ hours for new joiners.", e9, "L2", "P3 - Medium", now.minusDays(4), now.minusDays(3), "8h 30m", 8, "Resolved", "Auto-provisioning script fixed. Accounts now created in 15 minutes.", null, false),
            ticket("INC-1007", tlm, "Video consultation broken on iOS 17.4+. Patients cannot connect with doctors.", e6, "L3", "P1 - Critical", now.minusHours(2), null, null, 2, "Open", null, "200+ appointments affected", false),
            ticket("INC-1008", tlm, "Patient medical history not syncing between mobile app and web portal after v3.2 update.", e2, "L2", "P2 - High", now.minusHours(6), null, null, 6, "Escalated", null, "Awaiting vendor SDK patch", true),
            ticket("INC-1009", tlm, "Prescription module intermittently displaying wrong patient data. Data integrity concern.", e3, "L3", "P1 - Critical", now.minusHours(8), now.minusHours(1), "7h 0m", 7, "Resolved", "Race condition in session management fixed. Regression complete.", "HIPAA incident report filed", false),
            ticket("INC-1010", pay, "Salary slips not generating for 120 contract employees this month.", e5, "L2", "P1 - Critical", now.minusDays(5), now.minusDays(4), "20h 30m", 20, "Closed", "Payroll configuration updated for contract employment type.", null, false),
            ticket("INC-1011", pay, "TDS deduction incorrect for employees in new tax regime. Compliance risk.", e9, "L2", "P2 - High", now.minusDays(3), null, null, 36, "Pending", null, "Awaiting tax consultant confirmation", true),
            ticket("INC-1012", inv, "Stock count mismatch — 340 unit discrepancy between warehouse system and physical count.", e1, "L1", "P3 - Medium", now.minusHours(6), null, null, 6, "Open", null, "Quarterly audit triggered", false),
            ticket("INC-1013", inv, "Barcode scanner bulk import failing. Slowing down daily receiving operations.", e4, "L1", "P3 - Medium", now.minusDays(2), now.minusDays(1), "6h 15m", 6, "Resolved", "USB driver updated and scanners reconfigured.", null, false),
            ticket("INC-1014", cus, "Customers unable to track orders — status page returning 404 after last deployment.", e5, "L2", "P2 - High", now.minusHours(4), null, null, 4, "In Progress", null, "Post-deployment regression", false),
            ticket("INC-1015", cus, "Payment gateway timeout errors for credit card transactions above INR 50,000.", e9, "L2", "P1 - Critical", now.minusHours(1), null, null, 1, "Open", null, "Revenue impact — escalate immediately", false),
            ticket("INC-1016", dev, "Production CI/CD pipeline failing at Docker build stage. All releases blocked.", e10, "L3", "P1 - Critical", now.minusHours(3), now.minusMinutes(30), "2h 30m", 2, "Resolved", "Base Docker image updated and cache cleared. Pipeline green.", null, false),
            ticket("INC-1017", dev, "Kubernetes cluster memory at 94%. Risk of OOM pod evictions during peak hours.", e3, "L3", "P2 - High", now.minusHours(10), null, null, 10, "In Progress", null, "Node autoscaling being enabled", false)
        ));
        log.info("  → {} tickets seeded", ticketRepository.count());
    }

    private Ticket ticket(String num, Project proj, String desc, Employee emp,
                          String level, String priority,
                          LocalDateTime gen, LocalDateTime resp,
                          String resTime, int bizHours,
                          String status, String resolution,
                          String remarks, boolean slaBreached) {
        return Ticket.builder()
                .ticketNumber(num).project(proj).issueDescription(desc)
                .assignedEmployee(emp).supportLevel(level).priority(priority)
                .generationDatetime(gen).responseDatetime(resp)
                .resolutionTime(resTime).businessHoursElapsed(bizHours)
                .currentStatus(status).resolutionDetails(resolution)
                .remarks(remarks).slaBreached(slaBreached)
                .build();
    }
}
