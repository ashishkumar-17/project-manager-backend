package com.projectmanager.controller;

import com.projectmanager.entity.FileItem;
import com.projectmanager.service.FileService;
import com.projectmanager.service.Impl.FileServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping
    public ResponseEntity<List<FileItem>> getFiles(
            @RequestParam(value = "folderId", required = false) String folderId) {

        try {
            List<FileItem> files = fileService.getFiles(folderId);
            return ResponseEntity.ok(files);

        } catch (Exception e) {
            logger.error("Failed to retrieve files: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "parentId", required = false) String parentId,
            @RequestParam("uploaderId") String uploaderId) {

        Map<String, Object> response = new HashMap<>();

        try {
            logger.info("File upload request: {} by user: {}", file.getOriginalFilename(), uploaderId);

            // Use enhanced upload method if available
            if (fileService instanceof FileServiceImpl) {
                FileServiceImpl.FileUploadResult result =
                        ((FileServiceImpl) fileService).uploadFileWithResult(file, parentId, uploaderId);

                response.put("success", result.isSuccess());
                response.put("message", result.getMessage());
                response.put("fileSize", result.getFileSize());
                response.put("compressionRatio", result.getCompressionRatio());

                if (result.getCompressionRatio() > 0) {
                    response.put("compressionApplied", true);
                    response.put("spaceSaved", String.format("%.1f%%", result.getCompressionRatio()));
                }

                return result.isSuccess() ?
                        ResponseEntity.ok(response) :
                        ResponseEntity.badRequest().body(response);
            } else {
                // Fallback to standard upload
                fileService.uploadFile(file, parentId, uploaderId);
                response.put("success", true);
                response.put("message", "File uploaded successfully");
                response.put("fileSize", file.getSize());
                return ResponseEntity.ok(response);
            }

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid file upload request: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (IOException e) {
            logger.error("File upload failed: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "File upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        } catch (Exception e) {
            logger.error("Unexpected error during file upload: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/upload/multiple")
    public ResponseEntity<Map<String, Object>> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "parentId", required = false) String parentId,
            @RequestParam("uploaderId") String uploaderId) {

        Map<String, Object> response = new HashMap<>();
        Map<String, Map<String, Object>> results = new HashMap<>();
        int successCount = 0;
        int failCount = 0;

        for (MultipartFile file : files) {
            Map<String, Object> fileResult = new HashMap<>();
            try {
                if (fileService instanceof FileServiceImpl) {
                    FileServiceImpl.FileUploadResult result =
                            ((FileServiceImpl) fileService).uploadFileWithResult(file, parentId, uploaderId);

                    fileResult.put("success", result.isSuccess());
                    fileResult.put("message", result.getMessage());
                    fileResult.put("fileSize", result.getFileSize());
                    fileResult.put("compressionRatio", result.getCompressionRatio());

                    if (result.isSuccess()) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } else {
                    fileService.uploadFile(file, parentId, uploaderId);
                    fileResult.put("success", true);
                    fileResult.put("message", "File uploaded successfully");
                    fileResult.put("fileSize", file.getSize());
                    successCount++;
                }

            } catch (Exception e) {
                logger.error("Failed to upload file {}: {}", file.getOriginalFilename(), e.getMessage());
                fileResult.put("success", false);
                fileResult.put("message", e.getMessage());
                failCount++;
            }

            results.put(file.getOriginalFilename(), fileResult);
        }

        response.put("totalFiles", files.length);
        response.put("successCount", successCount);
        response.put("failCount", failCount);
        response.put("results", results);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/folder")
    public ResponseEntity<Map<String, Object>> createFolder(
            @RequestParam("folderName") String folderName,
            @RequestParam(value = "parentId", required = false) String parentId,
            @RequestParam("creatorId") String creatorId) {

        Map<String, Object> response = new HashMap<>();

        try {
            fileService.createFolder(folderName, parentId, creatorId);
            response.put("success", true);
            response.put("message", "Folder created successfully");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            logger.error("Failed to create folder: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to create folder");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteFileOrFolder(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();

        try {
            fileService.deleteFileOrFolder(id);
            response.put("success", true);
            response.put("message", "File/Folder deleted successfully");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (IOException e) {
            logger.error("Failed to delete file/folder: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to delete file/folder");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        } catch (Exception e) {
            logger.error("Unexpected error during deletion: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}