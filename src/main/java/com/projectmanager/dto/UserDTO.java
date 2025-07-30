package com.projectmanager.dto;

import java.time.LocalDate;

public class UserDTO {
    private String id;
    private String email;
    private String username;
    private String name;
    private String role;
    private boolean isOnline;
    private LocalDate lastSeen;
    private String timezone;
    private String avatar;

    public UserDTO(String id, String email, String username, String name, String role, boolean isOnline, LocalDate lastSeen, String timezone, String avatar) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.name = name;
        this.role = role;
        this.isOnline = isOnline;
        this.lastSeen = lastSeen;
        this.timezone = timezone;
        this.avatar = avatar;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public LocalDate getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(LocalDate lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
