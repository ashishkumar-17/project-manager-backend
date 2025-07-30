package com.projectmanager.mapper;

import com.projectmanager.dto.UserDTO;
import com.projectmanager.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

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
}
