package com.projectmanager.mapper;

import com.projectmanager.dto.RegisterRequest;
import com.projectmanager.dto.UserDTO;
import com.projectmanager.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class UserMapper {

    public final PasswordEncoder passwordEncoder;

    public UserMapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public UserDTO toDTO(User user){
        return new UserDTO(user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getName(),
                user.getRole().name(),
                user.isOnline(),
                user.getLastSeen(),
                user.getTimezone(),
                user.getAvatar());
    }

    public User toEntity(RegisterRequest request){
        return new User(
                request.getName(),
                request.getEmail(),
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                LocalDate.now(),
                false
        );
    }
}