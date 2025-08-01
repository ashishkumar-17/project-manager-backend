package com.projectmanager.service.Impl;

import com.projectmanager.entity.FileItem;
import com.projectmanager.repository.FileRepository;
import com.projectmanager.service.FileService;
import com.projectmanager.service.R2StorageService;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class FileServiceImpl implements FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    private final Tika tika = new Tika();
    private final FileRepository fileRepository;
    private final R2StorageService r2StorageService;

    @Value("${app.file.max-size}")
    private String maxFileSize;

    // File types that benefit from compression
    private static final Set<String> COMPRESSIBLE_TYPES = Set.of(
            "text/plain", "text/html", "text/css", "text/javascript", "text/csv",
            "application/json", "application/xml", "text/xml",
            "application/javascript", "application/pdf"
    );

    // Large file threshold (files larger than this will use special handling)
    private static final long LARGE_FILE_THRESHOLD = 10 * 1024 * 1024; // 10MB

    // File types that should not be compressed (already compressed)
    private static final Set<String> NO_COMPRESSION_TYPES = Set.of(
            "application/zip", "application/gzip", "application/x-rar-compressed",
            "application/x-7z-compressed", "video/mp4", "video/avi", "video/mov",
            "audio/mp3", "audio/aac", "audio/ogg"
    );

    public FileServiceImpl(FileRepository fileRepository, R2StorageService r2StorageService) {
        this.fileRepository = fileRepository;
        this.r2StorageService = r2StorageService;
    }

    @Override
    public void uploadFile(MultipartFile file, String parentId, String uploaderId) throws IOException {
        validateFile(file);

        String detectedType = tika.detect(file.getInputStream(), file.getOriginalFilename());
        logger.info("Uploading file: {} ({}), size: {} bytes, detected type: {}",
                file.getOriginalFilename(), uploaderId, file.getSize(), detectedType);

        // Generate unique file key
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String fileKey = generateFileKey(Objects.requireNonNull(file.getOriginalFilename()), uploaderId, fileExtension);

        try {
            // Get file bytes for processing
            byte[] fileBytes = file.getBytes();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);

            // Upload to R2 with appropriate strategy
            String fileUrl = uploadToR2(fileKey, inputStream, fileBytes.length, detectedType, file.getOriginalFilename());

            // Save file metadata to database
            FileItem item = createFileItem(file, detectedType, fileUrl, parentId, uploaderId);
            fileRepository.save(item);

            logger.info("File uploaded successfully: {} -> {}", file.getOriginalFilename(), fileUrl);

        } catch (Exception e) {
            logger.error("Failed to upload file {}: {}", file.getOriginalFilename(), e.getMessage());
            throw new IOException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    private String uploadToR2(String key, ByteArrayInputStream inputStream, long size,
                              String contentType, String originalFilename) {

        // Determine upload strategy based on file type and size
        if (isImageFile(contentType)) {
            logger.debug("Uploading image with compression: {}", originalFilename);
            return r2StorageService.uploadFile(key, inputStream, size, contentType);

        } else if (shouldCompress(contentType, size)) {
            logger.debug("Uploading compressible file: {}", originalFilename);
            return r2StorageService.uploadFile(key, inputStream, size, contentType);

        } else if (size > LARGE_FILE_THRESHOLD) {
            logger.debug("Uploading large file: {} ({} bytes)", originalFilename, size);
            return uploadLargeFile(key, inputStream, size, contentType);

        } else {
            logger.debug("Uploading file without compression: {}", originalFilename);
            return r2StorageService.uploadFile(key, inputStream, size, contentType);
        }
    }

    private String uploadLargeFile(String key, ByteArrayInputStream inputStream, long size, String contentType) {
        // For large files, you might want to implement multipart upload
        // For now, using standard upload but this can be enhanced
        return r2StorageService.uploadFile(key, inputStream, size, contentType);
    }

    private FileItem createFileItem(MultipartFile file, String detectedType, String url,
                                    String parentId, String uploaderId) throws IOException {
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

        return item;
    }

    @Override
    public void createFolder(String folderName, String parentId, String creatorId) {
        logger.info("Creating folder: {} by user: {}", folderName, creatorId);

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
        logger.info("Folder created successfully: {}", folderName);
    }

    @Override
    public List<FileItem> getFiles(String folderId) {
        if (folderId != null) {
            // Get files in specific folder
            return fileRepository.findByParentId(folderId);
        }
        // Get root level files (no parent)
        return fileRepository.findByParentIdIsNull();
    }

    @Override
    public void deleteFileOrFolder(String id) throws IOException {
        FileItem item = fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File/Folder not found"));

        logger.info("Deleting {}: {}", item.getType(), item.getName());

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
        logger.debug("Folder deleted: {}", folder.getName());
    }

    private void deleteFile(FileItem file) {
        // Delete from R2 storage asynchronously
        if (file.getUrl() != null) {
            deleteFromR2Async(file.getUrl(), file.getName());
        }

        // Delete from database
        fileRepository.delete(file);
        logger.debug("File deleted: {}", file.getName());
    }

    private void deleteFromR2Async(String fileUrl, String fileName) {
        CompletableFuture.runAsync(() -> {
            try {
                String key = extractKeyFromUrl(fileUrl);
                if (key != null) {
                    r2StorageService.deleteFile(key);
                    logger.debug("File deleted from R2: {}", key);
                }
            } catch (Exception e) {
                logger.warn("Failed to delete file from R2 {}: {}", fileName, e.getMessage());
            }
        }).exceptionally(throwable -> {
            logger.error("Async deletion failed for file {}: {}", fileName, throwable.getMessage());
            return null;
        });
    }

    // Helper methods
    private void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (file.getOriginalFilename() == null || file.getOriginalFilename().trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid filename");
        }

        // Parse max file size (e.g., "50MB" -> bytes)
        long maxBytes = parseFileSize(maxFileSize);
        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size: " + maxFileSize);
        }
    }

    private long parseFileSize(String sizeStr) {
        if (sizeStr == null || sizeStr.isEmpty()) {
            return 50 * 1024 * 1024; // Default 50MB
        }

        sizeStr = sizeStr.toUpperCase().trim();
        long multiplier = 1;

        if (sizeStr.endsWith("KB")) {
            multiplier = 1024;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        } else if (sizeStr.endsWith("MB")) {
            multiplier = 1024 * 1024;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        } else if (sizeStr.endsWith("GB")) {
            multiplier = 1024 * 1024 * 1024;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        }

        try {
            return Long.parseLong(sizeStr.trim()) * multiplier;
        } catch (NumberFormatException e) {
            return 50 * 1024 * 1024; // Default 50MB
        }
    }

    private String generateFileKey(String originalFilename, String uploaderId, String extension) {
        String sanitizedName = originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        return "file_" + uploaderId + "_" + uuid + "_" + sanitizedName;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private boolean isImageFile(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }

    private boolean shouldCompress(String contentType, long fileSize) {
        // Don't compress already compressed files
        if (NO_COMPRESSION_TYPES.contains(contentType)) {
            return false;
        }

        // Compress text-based files
        if (COMPRESSIBLE_TYPES.contains(contentType)) {
            return true;
        }

        // Compress text files based on content type prefix
        return contentType != null && (
                contentType.startsWith("text/") ||
                        contentType.startsWith("application/json") ||
                        contentType.startsWith("application/xml")
        );
    }

    private String extractKeyFromUrl(String url) {
        try {
            // Extract key from URL format: publicBase/key
            if (url.contains("/files/")) {
                int filesIndex = url.indexOf("/files/");
                return url.substring(filesIndex + 1); // +1 to remove leading slash
            }

            // Fallback: get everything after the last domain part
            String[] parts = url.split("/");
            if (parts.length >= 2) {
                return String.join("/", java.util.Arrays.copyOfRange(parts, parts.length - 3, parts.length));
            }
        } catch (Exception e) {
            logger.error("Failed to extract key from URL: {}", url, e);
        }
        return null;
    }

    // Additional utility methods for file management
    public FileUploadResult uploadFileWithResult(MultipartFile file, String parentId, String uploaderId) {
        try {
            long originalSize = file.getSize();
            uploadFile(file, parentId, uploaderId);

            // Get compression stats if available
            String detectedType = tika.detect(file.getInputStream(), file.getOriginalFilename());
            double compressionRatio = 0.0;

            if (r2StorageService instanceof R2StorageServiceImpl && shouldCompress(detectedType, originalSize)) {
                try {
                    R2StorageServiceImpl.CompressionStats stats =
                            ((R2StorageServiceImpl) r2StorageService).getCompressionStats(
                                    new ByteArrayInputStream(file.getBytes()), detectedType);
                    compressionRatio = stats.getCompressionRatio();
                } catch (Exception e) {
                    logger.debug("Could not get compression stats: {}", e.getMessage());
                }
            }

            return new FileUploadResult(true, "File uploaded successfully",
                    originalSize, compressionRatio);

        } catch (Exception e) {
            logger.error("File upload failed: {}", e.getMessage());
            return new FileUploadResult(false, e.getMessage(), file.getSize(), 0.0);
        }
    }

    // Result class for upload operations
    public static class FileUploadResult {
        private final boolean success;
        private final String message;
        private final long fileSize;
        private final double compressionRatio;

        public FileUploadResult(boolean success, String message, long fileSize, double compressionRatio) {
            this.success = success;
            this.message = message;
            this.fileSize = fileSize;
            this.compressionRatio = compressionRatio;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public long getFileSize() { return fileSize; }
        public double getCompressionRatio() { return compressionRatio; }
    }
}