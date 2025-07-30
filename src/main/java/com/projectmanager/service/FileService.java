package com.projectmanager.service;

import com.projectmanager.entity.FileItem;
import com.projectmanager.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileService {
    void uploadFile(MultipartFile file, String parentId, String uploaderId)  throws IOException;
    void createFolder(String folderName, String parentId, String creatorId);
    List<FileItem> getFiles(String folderId);

    void deleteFileOrFolder(String id) throws IOException;
}