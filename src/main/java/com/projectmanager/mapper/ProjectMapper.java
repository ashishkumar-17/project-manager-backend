package com.projectmanager.mapper;

import com.projectmanager.dto.ProjectResponseDTO;
import com.projectmanager.entity.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public ProjectResponseDTO ToDTO(Project project){
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
