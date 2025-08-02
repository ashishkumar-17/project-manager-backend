package com.projectmanager.service;

import com.projectmanager.dto.LoginRequest;
import com.projectmanager.dto.RegisterRequest;
import com.projectmanager.dto.UserDTO;
import com.projectmanager.entity.User;
import com.projectmanager.security.jwt.JwtAuthenticationResponse;

public interface AuthService {

    UserDTO getUser(String email);
    void registerUser(RegisterRequest request);
    JwtAuthenticationResponse loginUser(LoginRequest request);
    void logout(String userId);
    boolean existsByEmail(String email);
}