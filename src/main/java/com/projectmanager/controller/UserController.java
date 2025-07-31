package com.projectmanager.controller;

import com.projectmanager.dto.PasswordUpdateRequest;
import com.projectmanager.dto.UpdateProfileRequest;
import com.projectmanager.service.Impl.UserDetailsImpl;
import com.projectmanager.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

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
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Update failed",
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/me/password")
    public ResponseEntity<?> updatePassword(
            @RequestBody PasswordUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            userService.updatePassword(userDetails.getId(), request);
            return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Password update failed",
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/me/avatar")
    public ResponseEntity<?> uploadAvatar(
            @RequestParam("avatar") MultipartFile avatar,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            // Validate request
            if (avatar == null || avatar.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "No file provided",
                        "message", "Please select an avatar image to upload"
                ));
            }

            String avatarUrl = userService.uploadAvatar(userDetails.getId(), avatar);

            return ResponseEntity.ok(Map.of(
                    "message", "Avatar uploaded successfully",
                    "avatar", avatarUrl
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid file",
                    "message", e.getMessage()
            ));
        } catch (MultipartException e) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(Map.of(
                    "error", "File too large",
                    "message", "The uploaded file exceeds the maximum allowed size"
            ));
        } catch (Exception e) {
            // Log the full exception for debugging
            System.err.println("Avatar upload failed: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Upload failed",
                    "message", "An error occurred while uploading the avatar. Please try again."
            ));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            return ResponseEntity.ok(userService.getAllUser());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Failed to retrieve users",
                    "message", e.getMessage()
            ));
        }
    }

    // Global exception handler for multipart exceptions
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<?> handleMultipartException(MultipartException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(Map.of(
                "error", "File upload error",
                "message", "The uploaded file is too large or the request is malformed"
        ));
    }
}