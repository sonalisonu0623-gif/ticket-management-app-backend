package com.ticketsystem.service;

import com.ticketsystem.dto.request.LoginRequest;
import com.ticketsystem.dto.request.UserRequest;
import com.ticketsystem.dto.response.AuthResponse;
import com.ticketsystem.dto.response.UserResponse;

import java.util.List;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    UserResponse register(UserRequest request);
    AuthResponse refreshToken(String refreshToken);
}
