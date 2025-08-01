package com.projectmanager.service.Impl;

import com.projectmanager.dto.PasswordUpdateRequest;
import com.projectmanager.dto.UpdateProfileRequest;
import com.projectmanager.dto.UserDTO;
import com.projectmanager.entity.User;
import com.projectmanager.mapper.UserMapper;
import com.projectmanager.repository.UserRepository;
import com.projectmanager.service.R2StorageService;
import com.projectmanager.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    @Value("${cloudflare.r2.endpoint}")
    private String url;

    // Maximum file size: 5MB (before compression)
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

        // Generate unique filename with .jpg extension (since compression converts to JPEG)
        String fileName = "avatar_" + userId + "_" + UUID.randomUUID() + ".jpg";

        try {
            // Delete old avatar if exists (async, don't block upload)
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                deleteOldAvatarAsync(user.getAvatar());
            }

            // Create a buffered input stream that supports mark/reset for better error handling
            byte[] fileBytes = avatar.getBytes();
            logger.debug("Original avatar size for user {}: {} bytes", userId, fileBytes.length);

            java.io.ByteArrayInputStream bufferedStream = new java.io.ByteArrayInputStream(fileBytes);

            // Upload with automatic compression (images will be compressed automatically)
            String avatarUrl = r2StorageService.uploadFile(
                    fileName,
                    bufferedStream,
                    fileBytes.length,
                    avatar.getContentType()
            );

            // Update user record
            user.setAvatar(avatarUrl);
            userRepository.save(user);

            logger.info("Avatar uploaded successfully for user: {}", userId);
            return avatarUrl;
        } catch (IOException e) {
            logger.error("Failed to read avatar file for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to read avatar file: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Failed to upload avatar for user {}: {}", userId, e.getMessage());
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
        // Use CompletableFuture for better async handling
        CompletableFuture.runAsync(() -> {
            try {
                String oldKey = extractKeyFromUrl(avatarUrl);
                if (oldKey != null) {
                    r2StorageService.deleteFile(oldKey);
                    logger.debug("Successfully deleted old avatar: {}", oldKey);
                }
            } catch (Exception e) {
                logger.warn("Failed to delete old avatar {}: {}", avatarUrl, e.getMessage());
            }
        }).exceptionally(throwable -> {
            logger.error("Async deletion failed for avatar {}: {}", avatarUrl, throwable.getMessage());
            return null;
        });
    }

    private String extractKeyFromUrl(String url) {
        try {
            // Extract key from URL format: publicBase/key
            String key = url.substring(url.lastIndexOf('/') + 1);

            // Handle nested paths like "avatars/filename.jpg"
            if (url.contains("/avatars/")) {
                int avatarsIndex = url.indexOf("/avatars/");
                key = url.substring(avatarsIndex + 1); // +1 to remove leading slash
            }

            return key;
        } catch (Exception e) {
            logger.error("Failed to extract key from URL: {}", url, e);
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