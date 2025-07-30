package com.projectmanager.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class FileStorageUtil {

    private final Path rootLocation = Paths.get("uploads/files");

    public FileStorageUtil() throws IOException {
        Files.createDirectories(rootLocation);
    }

    public String store(MultipartFile file) throws IOException {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path destination = rootLocation.resolve(filename);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return destination.toString();
    }
}
