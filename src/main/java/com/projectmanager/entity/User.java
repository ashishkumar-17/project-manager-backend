package com.projectmanager.entity;

import com.projectmanager.service.IdGenerationService;
import com.projectmanager.util.ApplicationContextProvider;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {



    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false, unique = true)
    private String username;

    private String name;

    @Column(nullable = false)
    private String password;
    private String avatar;
    @Enumerated(EnumType.STRING)
    private Role role = Role.MEMBER;
    private boolean isOnline;
    private LocalDate lastSeen;
    private String timezone = "UTC";

    @OneToMany(mappedBy = "owner")
    private List<Project> projects;

    public enum Role{
        ADMIN,
        MANAGER,
        MEMBER
    }

    @PrePersist
    public void generateId() {
        if (this.id == null) {
            IdGenerationService idService = ApplicationContextProvider.getBean(IdGenerationService.class);
            this.id = idService.generateUserId();
        }
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        role = role;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }
}