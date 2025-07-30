package com.projectmanager.controller;

import com.projectmanager.dto.TaskRequestDTO;
import com.projectmanager.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> createTask(@RequestBody TaskRequestDTO dto){
        try{
            return ResponseEntity.ok(taskService.createTask(dto));
        } catch (Exception e){
            return ResponseEntity.badRequest().body("Creation failed: " + e.getMessage());
        }
    }

    @PutMapping("/update/{taskId}")
    public ResponseEntity<?> updateTask(@RequestBody TaskRequestDTO task, @PathVariable String taskId){
        try{
            return ResponseEntity.ok(taskService.updateTask(task, taskId));
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Task update failed: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable String taskId){
        try{
            taskService.deleteTask(taskId);
            return ResponseEntity.noContent().build();
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Deletion failed: " + e.getMessage());
        }
    }

    @PutMapping("{taskId}/markAsComplete")
    public ResponseEntity<?> markAsComplete(@PathVariable String taskId){
        try{
            taskService.markAsComplete(taskId);
            return ResponseEntity.noContent().build();
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Marking failed: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllTask(){
        try{
            return ResponseEntity.ok(taskService.getAllTask());
        }catch (Exception e){
            return ResponseEntity.badRequest().body("No Task Found" + e.getMessage());
        }
    }
}
