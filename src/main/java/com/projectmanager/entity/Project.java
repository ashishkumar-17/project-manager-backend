package com.projectmanager.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.projectmanager.dto.MemberDTO;
import com.projectmanager.service.IdGenerationService;
import com.projectmanager.util.ApplicationContextProvider;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "projects")
public class Project {
    @Id
    private String id;

    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PLANNING;

    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;

    private int progress = 0; // 0 - 100%

    private LocalDate startDate;
    private LocalDate endDate;

    @ElementCollection
    private List<String> members;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    @JsonIgnoreProperties("projects")
    private User owner;

    @ElementCollection
    private List<String> tags;

    private String color;


    public enum Status {
        PLANNING, ACTIVE, ON_HOLD, COMPLETED
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }

    @PrePersist
    public void generateId() {
        if (this.id == null) {
            IdGenerationService idService = ApplicationContextProvider.getBean(IdGenerationService.class);
            this.id = idService.generateProjectId();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}