package com.projectmanager.controller;

import com.projectmanager.dto.ProjectRequestDTO;
import com.projectmanager.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> createProject(@RequestBody ProjectRequestDTO dto) {
        try{
            return ResponseEntity.ok(projectService.createProject(dto));
        } catch (Exception e){
            return ResponseEntity.badRequest().body("Creation failed: " + e.getMessage());
        }
    }

    @PutMapping("/update/{projectId}")
    public ResponseEntity<?> updateProject(@RequestBody ProjectRequestDTO dto, @PathVariable String projectId){
        try{
            return ResponseEntity.ok(projectService.updateProject(projectId, dto));
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Update Failed: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllProjects(){
        try{
            return ResponseEntity.ok(projectService.getAllProjects());
        }catch (Exception e){
            return ResponseEntity.badRequest().body("No projects found: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{projectId}")
    public ResponseEntity<?> deleteProject(@PathVariable String projectId){
        try{
            projectService.deleteProject(projectId);
            return ResponseEntity.noContent().build();
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Deletion Failed: " + e.getMessage());
        }
    }
}
