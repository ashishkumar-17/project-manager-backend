package com.projectmanager.service;

import java.io.InputStream;

public interface R2StorageService {
    String uploadFile(String key, InputStream inputStream, long contentLength, String contentType);
    byte[] downloadFile(String key);
    void deleteFile(String key);
}
