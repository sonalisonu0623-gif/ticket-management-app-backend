package com.ticketsystem.service.impl;

import com.ticketsystem.dto.request.LoginRequest;
import com.ticketsystem.dto.request.UserRequest;
import com.ticketsystem.dto.response.AuthResponse;
import com.ticketsystem.dto.response.UserResponse;
import com.ticketsystem.entity.User;
import com.ticketsystem.exception.DuplicateResourceException;
import com.ticketsystem.repository.UserRepository;
import com.ticketsystem.security.CustomUserDetails;
import com.ticketsystem.security.JwtUtil;
import com.ticketsystem.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String accessToken = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        User user = userDetails.getUser();
        log.info("User {} logged in successfully", user.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .expiresIn(jwtUtil.getExpirationTime())
                .build();
    }

    @Override
    public UserResponse register(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .department(request.getDepartment())
                .role(request.getRole())
                .isActive(true)
                .build();

        user = userRepository.save(user);
        log.info("User registered: {}", user.getUsername());
        return mapToUserResponse(user);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        String username = jwtUtil.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        CustomUserDetails userDetails = new CustomUserDetails(user);

        if (!jwtUtil.isTokenValid(refreshToken, userDetails)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String newAccessToken = jwtUtil.generateToken(userDetails);
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .expiresIn(jwtUtil.getExpirationTime())
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId()).username(user.getUsername())
                .email(user.getEmail()).fullName(user.getFullName())
                .phone(user.getPhone()).department(user.getDepartment())
                .role(user.getRole()).isActive(user.getIsActive())
                .createdAt(user.getCreatedAt()).build();
    }
}
