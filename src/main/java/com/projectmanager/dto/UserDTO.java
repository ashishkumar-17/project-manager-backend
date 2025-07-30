package com.projectmanager.dto;

import com.projectmanager.entity.User;

public class UserDTO {
    private String id;
    private String email;
    private String username;
    private String name;
    private String avatar;

    public UserDTO(String id, String email, String name, String username, String avatar) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.username = username;
        this.avatar = avatar;
    }

    public static UserDTO from(User user) {
        return new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getUsername(),
                user.getAvatar()
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
