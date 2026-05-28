package com.ticketsystem.config;

import com.ticketsystem.entity.Employee;
import com.ticketsystem.entity.Employee.EmployeeStatus;
import com.ticketsystem.entity.Role;
import com.ticketsystem.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminDataSeeder implements ApplicationRunner {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder    passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (!employeeRepository.existsByUsername("admin")) {
            Employee admin = Employee.builder()
                    .username("admin")
                    .email("admin@nexus.local")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .employeeName("System Administrator")
                    .designation("Administrator")
                    .department("IT")
                    .status(EmployeeStatus.ACTIVE)
                    .isActive(true)
                    .build();
            employeeRepository.save(admin);
            log.info("Default admin account created — username: admin  password: admin123");
        }
    }
}
