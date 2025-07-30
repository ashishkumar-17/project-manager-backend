package com.projectmanager.service;

import com.projectmanager.dto.PasswordUpdateRequest;
import com.projectmanager.dto.UpdateProfileRequest;
import com.projectmanager.dto.UserDTO;
import com.projectmanager.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    UserDTO updateProfile(String userId, UpdateProfileRequest request);
    void updatePassword(String userId, PasswordUpdateRequest request);
    String uploadAvatar(String userId, MultipartFile avatar);

    List<UserDTO> getAllUser();
}
