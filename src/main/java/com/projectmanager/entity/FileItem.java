package com.projectmanager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.projectmanager.service.IdGenerationService;
import com.projectmanager.util.ApplicationContextProvider;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "fileItems")
public class FileItem {

    @Id
    private String id;

    private String name;
    private String type;

    @Column(name = "size")
    @JsonProperty("size")
    private long size;
    private String parentId;
    private String uploadedBy;
    private LocalDate uploadedAt;
    private String url;

    @PrePersist
    public void generateId() {
        if (this.id == null) {
            IdGenerationService idService = ApplicationContextProvider.getBean(IdGenerationService.class);
            this.id = idService.generateFileId();
        }
        if (this.uploadedAt == null) {
            this.uploadedAt = LocalDate.now();
        }
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("size")
    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @JsonProperty("parentId")
    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @JsonProperty("uploadedBy")
    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    @JsonProperty("uploadedAt")
    public LocalDate getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDate uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "FileItem{id='" + id + "', name='" + name + "', type='" + type + "', size=" + size + ", parentId='" + parentId + "', uploadedBy='" + uploadedBy + "', uploadedAt='" + uploadedAt + "', url='" + url + "'}";
    }
}
