package com.projectmanager.controller;

import com.projectmanager.dto.LoginRequest;
import com.projectmanager.dto.RegisterRequest;
import com.projectmanager.entity.User;
import com.projectmanager.service.Impl.AuthServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {


    private final AuthServiceImpl authService;

    public AuthController(AuthServiceImpl authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            if (authService.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body("Email already exists");
            }

            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());
            user.setUsername(request.getUsername());
            user.setName(request.getName());

            authService.registerUser(user);
            return ResponseEntity.ok("User registered successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(authService.loginUser(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Login failed: " + e.getMessage());
        }
    }

    @PutMapping("/logout/{userId}")
    public ResponseEntity<?> logout(@PathVariable String userId){
        try{
            authService.logout(userId);
            return ResponseEntity.ok("User Logout successfully");
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Logout failed: " + e.getMessage());
        }
    }
}