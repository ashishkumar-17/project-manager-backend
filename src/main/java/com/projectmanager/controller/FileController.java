package com.projectmanager.controller;

import com.projectmanager.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/api/files")
    public ResponseEntity<?> getFiles(@RequestParam(required = false) String folderId){
        try{
            return ResponseEntity.ok(fileService.getFiles(folderId));
        }catch (Exception e){
            return ResponseEntity.badRequest().body("file fetching failed: " + e.getMessage());
        }
    }

    @PostMapping("/api/files/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("uploaderId") String uploaderId,
            @RequestParam(name = "parentId", required = false) String parentId){

        try {
            fileService.uploadFile(file, parentId, uploaderId);
            return ResponseEntity.ok("file uploaded successfully!");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
        }
    }

    @PostMapping("/api/folders")
    public ResponseEntity<?> createFolder(@RequestParam("folder") String folderName,
                                          @RequestParam("creatorId") String creatorId,
                                          @RequestParam(name = "parentId", required = false) String parentId) {

        try {
            fileService.createFolder(folderName, parentId, creatorId);
            return ResponseEntity.ok("Folder created successfully!");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
        }
    }

    @DeleteMapping("/api/file/delete/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable String id) throws IOException {
        try{
            fileService.deleteFileOrFolder(id);
            return ResponseEntity.noContent().build();
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Deletion failed: " + e.getMessage());
        }
    }
}