package com.ticketsystem.serviceImpl;

import com.ticketsystem.dto.*;
import com.ticketsystem.entity.Employee;
import com.ticketsystem.entity.Role;
import com.ticketsystem.exception.ResourceNotFoundException;
import com.ticketsystem.repository.EmployeeRepository;
import com.ticketsystem.security.JwtService;
import com.ticketsystem.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final EmployeeRepository    employeeRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtService            jwtService;
    private final AuthenticationManager authenticationManager;

    // ── register ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ApiResponse<AuthResponse> register(RegisterRequest req) {
        if (employeeRepository.existsByEmail(req.getEmail()))
            throw new IllegalArgumentException("Email already registered: " + req.getEmail());
        if (employeeRepository.existsByUsername(req.getUsername()))
            throw new IllegalArgumentException("Username already taken: " + req.getUsername());

        Employee emp = Employee.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole() != null ? req.getRole() : Role.EMPLOYEE)
                .employeeName(req.getUsername())          // sensible default until profile is filled
                .status(Employee.EmployeeStatus.ACTIVE)
                .isActive(true)
                .build();

        employeeRepository.save(emp);
        log.info("Registered new employee: {}", emp.getUsername());

        String token = jwtService.generateToken(emp);
        return ApiResponse.success("Registration successful", buildAuthResponse(emp, token));
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Override
    public ApiResponse<AuthResponse> login(LoginRequest req) {
        // LoginRequest uses usernameOrEmail — try to resolve to a username first
        String principal = resolveUsername(req.getUsernameOrEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(principal, req.getPassword()));

        Employee emp = employeeRepository.findByUsername(principal)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal));

        String token = jwtService.generateToken(emp);
        log.info("Login successful: {}", emp.getUsername());
        return ApiResponse.success("Login successful", buildAuthResponse(emp, token));
    }

    // ── getProfile ────────────────────────────────────────────────────────────

    @Override
    public ApiResponse<UserProfileResponse> getProfile(String username) {
        Employee emp = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        UserProfileResponse profile = UserProfileResponse.builder()
                .id(emp.getId())
                .username(emp.getUsername())
                .email(emp.getEmail())
                .role(emp.getRole())               // Role enum — matches UserProfileResponse field type
                .employeeId(emp.getId())           // same as id for merged entity
                .isActive(emp.isActive())
                .createdAt(emp.getCreatedAt())
                .build();

        return ApiResponse.success("OK", profile);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /**
     * If the supplied value looks like an email, look up the username from DB.
     * Otherwise treat it as a username directly.
     */
    private String resolveUsername(String usernameOrEmail) {
        if (usernameOrEmail != null && usernameOrEmail.contains("@")) {
            return employeeRepository.findByEmail(usernameOrEmail)
                    .map(Employee::getUsername)
                    .orElse(usernameOrEmail);   // let Spring Security reject it with 401
        }
        return usernameOrEmail;
    }

    private AuthResponse buildAuthResponse(Employee emp, String token) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(emp.getId())
                .username(emp.getUsername())
                .email(emp.getEmail())
                .role(emp.getRole())              // Role enum — matches AuthResponse field type
                .employeeId(emp.getId())          // merged entity: employee IS the user
                .build();
    }
}
