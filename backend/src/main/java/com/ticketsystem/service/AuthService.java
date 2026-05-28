package com.ticketsystem.service;

import com.ticketsystem.dto.*;

public interface AuthService {

    ApiResponse<AuthResponse> register(RegisterRequest request);

    ApiResponse<AuthResponse> login(LoginRequest request);

    ApiResponse<UserProfileResponse> getProfile(String username);
}
