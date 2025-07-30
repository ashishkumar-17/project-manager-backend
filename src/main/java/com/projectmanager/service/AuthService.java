package com.projectmanager.service;

import com.projectmanager.dto.LoginRequest;
import com.projectmanager.entity.User;
import com.projectmanager.security.jwt.JwtAuthenticationResponse;

public interface AuthService {

    void registerUser(User user);
    JwtAuthenticationResponse loginUser(LoginRequest request);
}