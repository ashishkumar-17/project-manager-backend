package com.projectmanager.service.Impl;

import com.projectmanager.dto.ProjectRequestDTO;
import com.projectmanager.dto.ProjectResponseDTO;
import com.projectmanager.entity.Project;
import com.projectmanager.entity.User;
import com.projectmanager.repository.ProjectRepository;
import com.projectmanager.repository.TaskRepository;
import com.projectmanager.repository.UserRepository;
import com.projectmanager.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository,
                              UserRepository userRepository, TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    public ProjectResponseDTO createProject(ProjectRequestDTO dto) {
        Project project = new Project();

        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setStatus(Project.Status.valueOf(dto.getStatus()));
        project.setPriority(Project.Priority.valueOf(dto.getPriority()));
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());
        project.setTags(dto.getTags());
        project.setColor(dto.getColor());
        project.setMembers(dto.getMembers());

        User owner = userRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> new RuntimeException("Owner not found"));
        project.setOwner(owner);

        Project saved = projectRepository.save(project);
        return mapToDTO(saved);
    }

    public ProjectResponseDTO updateProject(String id, ProjectRequestDTO updatedProject) {
        Project existing = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        existing.setName(updatedProject.getName());
        existing.setDescription(updatedProject.getDescription());
        existing.setStatus(Project.Status.valueOf(updatedProject.getStatus()));
        existing.setPriority(Project.Priority.valueOf(updatedProject.getPriority()));
        existing.setStartDate(updatedProject.getStartDate());
        existing.setEndDate(updatedProject.getEndDate());
        existing.setTags(updatedProject.getTags());
        existing.setColor(updatedProject.getColor());
        projectRepository.save(existing);
        return mapToDTO(existing);
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll()
                .stream().toList();
    }

    @Override
    public void deleteProject(String projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project does not exist"));

        taskRepository.deleteAllByProjectId(projectId);
        projectRepository.delete(project);
    }

    public ProjectResponseDTO mapToDTO(Project project){
        return new ProjectResponseDTO(project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus().name(),
                project.getPriority().name(),
                project.getProgress(),
                project.getStartDate(),
                project.getEndDate(),
                project.getMembers(),
                project.getOwner().getId(),
                project.getTags(),
                project.getColor());
    }
}