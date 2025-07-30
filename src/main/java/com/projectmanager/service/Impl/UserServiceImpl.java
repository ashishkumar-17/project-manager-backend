package com.projectmanager.service.Impl;

import com.projectmanager.dto.PasswordUpdateRequest;
import com.projectmanager.dto.UpdateProfileRequest;
import com.projectmanager.dto.UserDTO;
import com.projectmanager.entity.User;
import com.projectmanager.repository.UserRepository;
import com.projectmanager.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDTO updateProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setTimezone(request.getTimezone());
        return UserDTO.from(userRepository.save(user));
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

        String fileName = UUID.randomUUID() + "_" + avatar.getOriginalFilename();
        Path uploadPath = Paths.get("uploads/avatars/");

        try {
            Files.createDirectories(uploadPath);
            Files.copy(avatar.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload avatar", e);
        }

        String avatarUrl = "/uploads/avatars/" + fileName;
        user.setAvatar(avatarUrl);
        userRepository.save(user);
        return avatarUrl;
    }

    @Override
    public List<User> getAllUser() {
        return userRepository.findAll().stream()
                .toList();
    }

    @Override
    public User getUser(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("user not found"));
    }

}
