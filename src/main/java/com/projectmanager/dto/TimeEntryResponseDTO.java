package com.projectmanager.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TimeEntryResponseDTO {
    private String id;
    private String taskId;
    private String userId;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int duration;
    private LocalDate date;

    public TimeEntryResponseDTO(String id, String taskId, String userId, String description, LocalDateTime startTime, LocalDateTime endTime, int duration, LocalDate date) {
        this.id = id;
        this.taskId = taskId;
        this.userId = userId;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.date = date;
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
