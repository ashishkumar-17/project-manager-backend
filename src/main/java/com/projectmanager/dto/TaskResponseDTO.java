package com.projectmanager.dto;

import java.time.LocalDate;
import java.util.List;

public class TaskResponseDTO {
    private String id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String assignee;
    private String reporter;
    private String projectId;
    private LocalDate dueDate;
    private int estimatedHours;
    private List<String> tags;

    public TaskResponseDTO(String id, String title, String description, String status, String priority, String assignee, String reporter, String projectId, LocalDate dueDate, int estimatedHours, List<String> tags) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.assignee = assignee;
        this.reporter = reporter;
        this.projectId = projectId;
        this.dueDate = dueDate;
        this.estimatedHours = estimatedHours;
        this.tags = tags;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assigneeId) {
        this.assignee = assigneeId;
    }

    public String getReporter() {
        return reporter;
    }


    public void setReporter(String reporterId) {
        this.reporter = reporterId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public int getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(int estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
