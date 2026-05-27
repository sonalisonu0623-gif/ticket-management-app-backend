package com.ticketsystem.service;

import com.ticketsystem.config.JwtTokenProvider;
import com.ticketsystem.dto.*;
import com.ticketsystem.entity.User;
import com.ticketsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public UserDTO registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username registration mismatch: Identifier exists.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email registry index tracking fault: Address exists.");
        }

        String targetRole = "USER";
        if (request.getRole() != null && (request.getRole().equalsIgnoreCase("ADMIN") || request.getRole().equalsIgnoreCase("ROLE_ADMIN"))) {
            targetRole = "ADMIN";
        }

        User user = User.builder()
                .username(request.getUsername().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(targetRole)
                .build();

        return toDTO(userRepository.save(user));
    }

    public AuthResponse authenticateUser(LoginRequest request) {
        // Resolve lookup against either username or email metrics
        User resolvedUser = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Target secure context entry tracking absent."));

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        resolvedUser.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .token(jwt)
                .user(toDTO(resolvedUser))
                .build();
    }

    public UserDTO getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new IllegalStateException("Active user tracking block missing inside system runtime mapping.");
        }
        
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No valid record matching profile metrics."));
        return toDTO(user);
    }

    public UserDTO toDTO(User u) {
        return UserDTO.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .role(u.getRole())
                .createdAt(u.getCreatedAt())
                .build();
    }
}apackage com.ticketsystem.service;

import com.ticketsystem.config.JwtTokenProvider;
import com.ticketsystem.dto.*;
import com.ticketsystem.entity.User;
import com.ticketsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    private static final Set<String> VALID_ROLES = Set.of(
        "ADMIN", "PROJECT_MANAGER", "L1_SUPPORT", "L2_SUPPORT", "L3_SUPPORT", "USER"
    );

    @Transactional
    public UserDTO registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email address already registered.");
        }

        // Validate and resolve role
        String role = resolveRole(request.getRole());

        User user = User.builder()
                .username(request.getUsername().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .isActive(true)
                .build();

        return toDTO(userRepository.save(user));
    }

    public AuthResponse authenticateUser(LoginRequest request) {
        User resolvedUser = userRepository
                .findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials."));

        if (!resolvedUser.getIsActive()) {
            throw new IllegalStateException("Account is deactivated. Contact your administrator.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        resolvedUser.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .token(jwt)
                .user(toDTO(resolvedUser))
                .build();
    }

    public UserDTO getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new IllegalStateException("No authenticated user in current session.");
        }
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
        return toDTO(user);
    }

    private String resolveRole(String requested) {
        if (requested == null || requested.isBlank()) return "USER";
        String upper = requested.toUpperCase().replace("ROLE_", "");
        return VALID_ROLES.contains(upper) ? upper : "USER";
    }

    public UserDTO toDTO(User u) {
        return UserDTO.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .role(u.getRole())
                .isActive(u.getIsActive())
                .createdAt(u.getCreatedAt())
                .build();
    }
}