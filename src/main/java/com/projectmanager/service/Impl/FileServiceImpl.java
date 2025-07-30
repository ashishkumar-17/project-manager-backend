package com.projectmanager.service.Impl;

import com.projectmanager.entity.FileItem;
import com.projectmanager.repository.FileRepository;
import com.projectmanager.service.FileService;
import com.projectmanager.util.FileStorageUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.tika.Tika;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

@Service
public class FileServiceImpl implements FileService {

    private final Tika tika = new Tika();
    private final FileRepository fileRepository;
    private final FileStorageUtil fileStorageUtil;

    public FileServiceImpl(FileRepository fileRepository, FileStorageUtil fileStorageUtil) {
        this.fileRepository = fileRepository;
        this.fileStorageUtil = fileStorageUtil;
    }

    @Override
    public void uploadFile(MultipartFile file, String parentId, String uploaderId) throws IOException {
        String detectedType = tika.detect(file.getInputStream(), file.getOriginalFilename());
        String url = fileStorageUtil.store(file);

        FileItem item = new FileItem();
        item.setName(file.getOriginalFilename());
        item.setType(detectedType);
        item.setSize(file.getSize());
        item.setUploadedBy(uploaderId);
        item.setUploadedAt(LocalDate.now());
        item.setUrl(url);

        if (parentId != null) {
            FileItem parent = fileRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid parent ID"));
            item.setParentId(parentId);
        }

        fileRepository.save(item);
    }

    @Override
    public void createFolder(String folderName, String parentId, String creatorId) {
        FileItem folder = new FileItem();
        folder.setName(folderName);
        folder.setType("folder");
        folder.setUploadedBy(creatorId);
        folder.setUploadedAt(LocalDate.now());

        if (parentId != null) {
            FileItem parent = fileRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid parent ID"));
            folder.setParentId(parentId);
        }
        fileRepository.save(folder);
    }

    @Override
    public List<FileItem> getFiles(String folderId) {
        if (folderId != null) {
            return fileRepository.findById(folderId).stream().toList();
        }
        return fileRepository.findAll().stream()
                .toList();
    }

    @Override
    public void deleteFileOrFolder(String id) throws IOException {
        FileItem item = fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File/Folder not found"));

        if ("folder".equals(item.getType())) {
            deleteFolderRecursively(item);
        } else {
            deleteFile(item);
        }
    }

    private void deleteFolderRecursively(FileItem folder) throws IOException {
        List<FileItem> children = fileRepository.findByParentId(folder.getId());
        for (FileItem child : children) {
            if ("folder".equals(child.getType())) {
                deleteFolderRecursively(child);
            } else {
                deleteFile(child);
            }
        }
        fileRepository.delete(folder);
    }

    private void deleteFile(FileItem file) throws IOException {
        if (file.getUrl() != null) {
            Path path = Paths.get(file.getUrl());
            Files.deleteIfExists(path);
        }
        fileRepository.delete(file);
    }
}