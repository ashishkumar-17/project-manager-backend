package com.projectmanager.entity;

import com.projectmanager.service.IdGenerationService;
import com.projectmanager.util.ApplicationContextProvider;
import jakarta.persistence.*;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "timeEntries")
public class TimeEntry {

    @Id
    private String id;

    private String taskId;
    private String userId;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int duration;
    private LocalDate date;

    @PrePersist
    public void generateId() {
        if (this.id == null) {
            IdGenerationService idService = ApplicationContextProvider.getBean(IdGenerationService.class);
            this.id = idService.generateTimeEntryId();
        }
        if (this.date == null) {
            this.date = LocalDate.now();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
