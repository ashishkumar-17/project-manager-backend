package com.projectmanager.service.Impl;

import com.projectmanager.dto.PasswordUpdateRequest;
import com.projectmanager.dto.UpdateProfileRequest;
import com.projectmanager.dto.UserDTO;
import com.projectmanager.entity.User;
import com.projectmanager.mapper.UserMapper;
import com.projectmanager.repository.UserRepository;
import com.projectmanager.service.R2StorageService;
import com.projectmanager.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    @Value("${cloudflare.r2.endpoint}")
    private String url;

    // Maximum file size: 5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    // Allowed file types
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private final R2StorageService r2StorageService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserServiceImpl(R2StorageService r2StorageService, UserRepository userRepository,
                           PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.r2StorageService = r2StorageService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    public UserDTO updateProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setTimezone(request.getTimezone());
        return userMapper.toDTO(userRepository.save(user));
    }

    @Override
    public void updatePassword(String userId, PasswordUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public String uploadAvatar(String userId, MultipartFile avatar) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate file
        validateAvatarFile(avatar);

        // Generate unique filename
        String originalFilename = avatar.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String fileName = "avatar_" + userId + "_" + UUID.randomUUID() + extension;

        try {
            // Delete old avatar if exists (async, don't block upload)
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                deleteOldAvatarAsync(user.getAvatar());
            }

            // Single upload attempt - let S3Client handle retries
            String avatarUrl = r2StorageService.uploadFile(
                    "avatars/" + fileName,
                    avatar.getInputStream(),
                    avatar.getSize(),
                    avatar.getContentType()
            );

            // Update user record
            user.setAvatar(avatarUrl);
            userRepository.save(user);

            return avatarUrl;

        } catch (IOException e) {
            throw new RuntimeException("Failed to read avatar file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload avatar: " + e.getMessage(), e);
        }
    }

    private void validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Avatar file cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Avatar file size cannot exceed 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file type. Only JPEG, PNG, GIF, and WebP images are allowed");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid filename");
        }
    }

    private void deleteOldAvatarAsync(String avatarUrl) {
        // Run deletion in background thread to not block upload
        new Thread(() -> {
            try {
                String oldKey = extractKeyFromUrl(avatarUrl);
                if (oldKey != null) {
                    r2StorageService.deleteFile(oldKey);
                }
            } catch (Exception e) {
                System.err.println("Failed to delete old avatar: " + e.getMessage());
            }
        }).start();
    }

    private String extractKeyFromUrl(String url) {
        try {
            // Extract key from URL format: publicBase/bucket/key
            String[] parts = url.split("/");
            if (parts.length >= 2) {
                // Return the last two parts joined with "/"
                return parts[parts.length - 2] + "/" + parts[parts.length - 1];
            }
        } catch (Exception e) {
            System.err.println("Failed to extract key from URL: " + url);
        }
        return null;
    }

    @Override
    public List<UserDTO> getAllUser() {
        return userRepository.findAll().stream()
                .map(userMapper::toDTO)
                .toList();
    }
}