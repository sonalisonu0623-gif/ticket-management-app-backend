package com.ticketsystem.config;

import com.ticketsystem.entity.Employee;
import com.ticketsystem.entity.Project;
import com.ticketsystem.entity.Ticket;
import com.ticketsystem.repository.EmployeeRepository;
import com.ticketsystem.repository.ProjectRepository;
import com.ticketsystem.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;
    private final TicketRepository ticketRepository;

    @Override
    public void run(String... args) {
        if (projectRepository.count() > 0) {
            log.info("Sample data already present — skipping seed.");
            return;
        }

        log.info("Seeding initial data...");

        // Projects
        List<Project> projects = projectRepository.saveAll(List.of(
                Project.builder().projectName("HR Portal").build(),
                Project.builder().projectName("ERP System").build(),
                Project.builder().projectName("Telemedicine").build(),
                Project.builder().projectName("Payroll System").build(),
                Project.builder().projectName("Inventory Management").build()
        ));

        // Employees
        List<Employee> employees = employeeRepository.saveAll(List.of(
                Employee.builder().employeeName("John.D").supportLevel("L1").build(),
                Employee.builder().employeeName("Smith.K").supportLevel("L2").build(),
                Employee.builder().employeeName("David.R").supportLevel("L3").build(),
                Employee.builder().employeeName("Maria.T").supportLevel("L1").build(),
                Employee.builder().employeeName("Alex.P").supportLevel("L2").build(),
                Employee.builder().employeeName("Chen.W").supportLevel("L3").build()
        ));

        // Tickets
        LocalDateTime now = LocalDateTime.now();

        ticketRepository.saveAll(List.of(
                Ticket.builder()
                        .ticketNumber("INC-1001")
                        .project(projects.get(0))
                        .issueDescription("Unable to login to HR portal. Password reset not working for multiple users.")
                        .assignedEmployee(employees.get(0))
                        .supportLevel("L1").priority("P2 - High")
                        .generationDatetime(now.minusDays(3))
                        .responseDatetime(now.minusDays(2))
                        .resolutionTime("24h 0m")
                        .currentStatus("Resolved")
                        .resolutionDetails("Reset password policy updated and users notified.")
                        .remarks("Affected 15 users in HR department")
                        .build(),

                Ticket.builder()
                        .ticketNumber("INC-1002")
                        .project(projects.get(1))
                        .issueDescription("ERP module crashing on report generation. Error occurs for reports with more than 500 rows.")
                        .assignedEmployee(employees.get(2))
                        .supportLevel("L3").priority("P1 - Critical")
                        .generationDatetime(now.minusDays(2))
                        .currentStatus("In Progress")
                        .remarks("Critical for month-end closing")
                        .build(),

                Ticket.builder()
                        .ticketNumber("INC-1003")
                        .project(projects.get(2))
                        .issueDescription("Video consultation feature not working on iOS devices. Patients unable to connect.")
                        .assignedEmployee(employees.get(1))
                        .supportLevel("L2").priority("P2 - High")
                        .generationDatetime(now.minusDays(1))
                        .currentStatus("Open")
                        .remarks("Patients affected")
                        .build(),

                Ticket.builder()
                        .ticketNumber("INC-1004")
                        .project(projects.get(3))
                        .issueDescription("Salary slips not generating for contract employees this month.")
                        .assignedEmployee(employees.get(1))
                        .supportLevel("L2").priority("P1 - Critical")
                        .generationDatetime(now.minusDays(5))
                        .responseDatetime(now.minusDays(4))
                        .resolutionTime("20h 30m")
                        .currentStatus("Closed")
                        .resolutionDetails("Payroll configuration updated for contract type employees.")
                        .build(),

                Ticket.builder()
                        .ticketNumber("INC-1005")
                        .project(projects.get(4))
                        .issueDescription("Inventory stock count mismatch between warehouse system and physical count.")
                        .assignedEmployee(employees.get(0))
                        .supportLevel("L1").priority("P3 - Medium")
                        .generationDatetime(now.minusHours(6))
                        .currentStatus("Open")
                        .remarks("Warehouse team flagged")
                        .build(),

                Ticket.builder()
                        .ticketNumber("INC-1006")
                        .project(projects.get(0))
                        .issueDescription("Performance issue — employee search taking over 30 seconds to return results.")
                        .assignedEmployee(employees.get(2))
                        .supportLevel("L3").priority("P3 - Medium")
                        .generationDatetime(now.minusDays(4))
                        .responseDatetime(now.minusDays(3))
                        .resolutionTime("18h 45m")
                        .currentStatus("Resolved")
                        .resolutionDetails("Database indexes optimized. Query time reduced to under 1 second.")
                        .build(),

                Ticket.builder()
                        .ticketNumber("INC-1007")
                        .project(projects.get(1))
                        .issueDescription("Purchase order approval workflow stuck — email notifications not being sent to approvers.")
                        .assignedEmployee(employees.get(1))
                        .supportLevel("L2").priority("P2 - High")
                        .generationDatetime(now.minusHours(12))
                        .currentStatus("In Progress")
                        .remarks("Finance team blocked")
                        .build(),

                Ticket.builder()
                        .ticketNumber("INC-1008")
                        .project(projects.get(2))
                        .issueDescription("Patient data not syncing between mobile app and web portal after latest app update.")
                        .assignedEmployee(employees.get(2))
                        .supportLevel("L3").priority("P1 - Critical")
                        .generationDatetime(now.minusHours(2))
                        .currentStatus("On Hold")
                        .remarks("Waiting for vendor response")
                        .build()
        ));

        log.info("Seeded {} projects, {} employees, 8 tickets.", projects.size(), employees.size());
    }
}
