package com.projectmanager.service.Impl;

import com.projectmanager.dto.TaskRequestDTO;
import com.projectmanager.dto.TaskResponseDTO;
import com.projectmanager.entity.Project;
import com.projectmanager.entity.Task;
import com.projectmanager.entity.User;
import com.projectmanager.repository.ProjectRepository;
import com.projectmanager.repository.TaskRepository;
import com.projectmanager.repository.UserRepository;
import com.projectmanager.service.TaskService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public TaskServiceImpl(UserRepository userRepository, ProjectRepository projectRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public TaskResponseDTO createTask(TaskRequestDTO dto) {
        Task task = new Task();

        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(Task.Status.valueOf(dto.getStatus()));
        task.setPriority(Task.Priority.valueOf(dto.getPriority()));
        task.setDueDate(dto.getDueDate());
        task.setTags(dto.getTags());
        task.setEstimatedHours(dto.getEstimatedHours());

        User assignee = userRepository.findById(dto.getAssignee())
                .orElseThrow(() -> new RuntimeException("assignee not found"));
        User reporter = userRepository.findById(dto.getReporter())
                .orElseThrow(() -> new RuntimeException("reporter not found"));
        Project project = projectRepository.findById(dto.getProjectId())
                        .orElseThrow(() -> new RuntimeException("project not found"));
        task.setAssignee(assignee);
        task.setReporter(reporter);
        task.setProject(project);

        Task saved = taskRepository.save(task);

        return mapToDTO(saved);
    }

    public TaskResponseDTO updateTask(TaskRequestDTO task, String taskId){
        Task existing = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task update failed."));

        existing.setTitle(task.getTitle());
        existing.setDescription(task.getDescription());
        existing.setStatus(Task.Status.valueOf(task.getStatus()));
        existing.setPriority(Task.Priority.valueOf(task.getPriority()));
        existing.setAssignee(userRepository.findById(task.getAssignee()).get());
        existing.setReporter(userRepository.findById(task.getReporter()).get());
        existing.setProject(projectRepository.findById(task.getProjectId()).get());
        existing.setDueDate(task.getDueDate());
        existing.setEstimatedHours(task.getEstimatedHours());
        existing.setTags(task.getTags());

        taskRepository.save(existing);
        return mapToDTO(existing);
    }

    @Override
    public List<Task> getAllTask() {
        return taskRepository.findAll()
                .stream().toList();
    }

    @Override
    public void deleteTask(String taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Deletion Failed"));

        taskRepository.delete(task);
    }

    @Override
    public void markAsComplete(String taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Marking Failed"));

        task.setStatus(Task.Status.DONE);
        taskRepository.save(task);
    }

    public TaskResponseDTO mapToDTO(Task task){
        return new TaskResponseDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus().name(),
                task.getPriority().name(),
                task.getAssignee().getId(),
                task.getReporter().getId(),
                task.getProject().getId(),
                task.getDueDate(),
                task.getEstimatedHours(),
                task.getTags()
        );
    }
}