package com.projectmanager.service;

import com.projectmanager.dto.TaskRequestDTO;
import com.projectmanager.dto.TaskResponseDTO;
import com.projectmanager.entity.Project;
import com.projectmanager.entity.Task;

import java.util.List;


public interface TaskService {
    TaskResponseDTO createTask(TaskRequestDTO dto);
    TaskResponseDTO updateTask(TaskRequestDTO task, String taskId);

    List<Task> getAllTask();

    void deleteTask(String taskId);

    void markAsComplete(String taskId);
}
