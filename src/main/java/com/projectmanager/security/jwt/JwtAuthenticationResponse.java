package com.projectmanager.security.jwt;

public class JwtAuthenticationResponse {
    private String token;
    private String id;
    private String email;
    private String username;
    private String name;
    private String role;
    private String avatar;
    private String timezone;

    public JwtAuthenticationResponse(String token, String id, String email, String username, String name, String role, String avatar, String timezone) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.username = username;
        this.name = name;
        this.role = role;
        this.avatar = avatar;
        this.timezone = timezone;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
