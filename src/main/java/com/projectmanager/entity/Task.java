package com.projectmanager.entity;

import com.projectmanager.service.IdGenerationService;
import com.projectmanager.util.ApplicationContextProvider;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    private String id;
    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private Status status = Status.TODO;
    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;

    @ManyToOne
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @ManyToOne
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    private LocalDate dueDate;
    private LocalDate createdAt;
    private LocalDate updatedAt;

    @ElementCollection
    private List<String> tags;
    private int estimatedHours;
    private int loggedHours;

    @ElementCollection
    private List<String> dependencies;

    public enum Status {
        TODO, IN_PROGRESS, REVIEW , DONE
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            IdGenerationService idService = ApplicationContextProvider.getBean(IdGenerationService.class);
            this.id = idService.generateTaskId();
        }
        LocalDate now = LocalDate.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDate.now();
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

    public User getAssignee() {
        return assignee;
    }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    public User getReporter() {
        return reporter;
    }

    public void setReporter(User reporter) {
        this.reporter = reporter;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public int getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(int estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public int getLoggedHours() {
        return loggedHours;
    }

    public void setLoggedHours(int loggedHours) {
        this.loggedHours = loggedHours;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }
}
