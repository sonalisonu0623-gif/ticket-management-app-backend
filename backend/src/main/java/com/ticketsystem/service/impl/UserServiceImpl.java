package com.ticketsystem.service.impl;

import com.ticketsystem.dto.request.UserRequest;
import com.ticketsystem.dto.response.UserResponse;
import com.ticketsystem.entity.Role;
import com.ticketsystem.entity.User;
import com.ticketsystem.exception.DuplicateResourceException;
import com.ticketsystem.exception.ResourceNotFoundException;
import com.ticketsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(String role) {
        return userRepository.findByRole(Role.valueOf(role.toUpperCase()))
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getSupportEngineers() {
        return userRepository.findByRoleAndIsActive(Role.SUPPORT_ENGINEER, true)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return mapToResponse(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id)));
    }

    public UserResponse createUser(UserRequest request) {
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

        return mapToResponse(userRepository.save(user));
    }

    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (!user.getUsername().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setDepartment(request.getDepartment());
        user.setRole(request.getRole());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return mapToResponse(userRepository.save(user));
    }

    public void toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setIsActive(!user.getIsActive());
        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) throw new ResourceNotFoundException("User", id);
        userRepository.deleteById(id);
    }

    private UserResponse mapToResponse(User u) {
        return UserResponse.builder()
                .id(u.getId()).username(u.getUsername()).email(u.getEmail())
                .fullName(u.getFullName()).phone(u.getPhone()).department(u.getDepartment())
                .role(u.getRole()).isActive(u.getIsActive()).createdAt(u.getCreatedAt()).build();
    }
}
