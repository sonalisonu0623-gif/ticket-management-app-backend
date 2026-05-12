package com.ticketsystem.controller;

import com.ticketsystem.dto.request.LoginRequest;
import com.ticketsystem.dto.request.UserRequest;
import com.ticketsystem.dto.response.ApiResponse;
import com.ticketsystem.dto.response.AuthResponse;
import com.ticketsystem.dto.response.UserResponse;
import com.ticketsystem.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login successful", authService.login(request)));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", authService.register(request)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@RequestParam String refreshToken) {
        return ResponseEntity.ok(ApiResponse.success(authService.refreshToken(refreshToken)));
    }
}
