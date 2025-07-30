package com.projectmanager.controller;

import com.projectmanager.dto.PasswordUpdateRequest;
import com.projectmanager.dto.UpdateProfileRequest;
import com.projectmanager.service.Impl.UserDetailsImpl;
import com.projectmanager.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(
            @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            return ResponseEntity.ok(userService.updateProfile(userDetails.getId(), request));
        }catch (Exception e) {
            return ResponseEntity.badRequest().body("Update failed: " + e.getMessage());
        }

    }

    @PutMapping("/me/password")
    public ResponseEntity<?> updatePassword(
            @RequestBody PasswordUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            userService.updatePassword(userDetails.getId(), request);
            return ResponseEntity.ok("password updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Update failed: " + e.getMessage());
        }
    }

    @PostMapping("/me/avatar")
    public ResponseEntity<?> uploadAvatar(
            @RequestParam("avatar") MultipartFile avatar,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            return ResponseEntity.ok(Map.of("avatar", userService.uploadAvatar(userDetails.getId(), avatar)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Update failed: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers(){
        try{
            return ResponseEntity.ok(userService.getAllUser());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Update failed: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable String id){
        try{
            return ResponseEntity.ok(userService.getUser(id));
        }catch (Exception e){
            return ResponseEntity.badRequest().body("User not found: " + e.getMessage());
        }
    }
}
