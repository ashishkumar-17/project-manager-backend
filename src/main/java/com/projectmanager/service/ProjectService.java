package com.projectmanager.service;

import com.projectmanager.dto.ProjectRequestDTO;
import com.projectmanager.dto.ProjectResponseDTO;
import com.projectmanager.entity.Project;

import java.util.List;

public interface ProjectService {
    ProjectResponseDTO createProject(ProjectRequestDTO project);

    ProjectResponseDTO updateProject(String id, ProjectRequestDTO project);

    List<Project> getAllProjects();

    void deleteProject(String projectId);
}