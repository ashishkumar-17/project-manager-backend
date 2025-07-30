package com.projectmanager.dto;

import java.time.LocalDate;
import java.util.List;

public class ProjectResponseDTO {
    private String id;
    private String name;
    private String description;
    private String status;
    private String priority;
    private int progress;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> members;
    private String ownerId;
    private List<String> tags;
    private String color;

    public ProjectResponseDTO(String id, String name, String description, String status, String priority, int progress, LocalDate startDate, LocalDate endDate, List<String> members, String ownerId, List<String> tags, String color) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.progress = progress;
        this.startDate = startDate;
        this.endDate = endDate;
        this.members = members;
        this.ownerId = ownerId;
        this.tags = tags;
        this.color = color;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
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

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
