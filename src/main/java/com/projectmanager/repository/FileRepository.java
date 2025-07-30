package com.projectmanager.repository;

import com.projectmanager.entity.FileItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<FileItem, String> {
    List<FileItem> findByParentId(String parentId);
    List<FileItem> findByNameContainingIgnoreCaseAndParentId(String name, String parentId);
    List<FileItem> findByNameContainingIgnoreCaseAndParentIdIsNull(String name);
}
