package com.projectmanager.repository;

import com.projectmanager.entity.Task;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {

    @Modifying
    @Transactional
    @Query("DELETE FROM Task e WHERE e.project.id = :projectId")
    void deleteAllByProjectId(@Param("projectId") String projectId);
}
