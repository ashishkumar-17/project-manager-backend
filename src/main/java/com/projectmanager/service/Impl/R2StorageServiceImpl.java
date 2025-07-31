package com.projectmanager.service.Impl;

import com.projectmanager.service.R2StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;

@Service
public class R2StorageServiceImpl implements R2StorageService {

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    @Value("${cloudflare.r2.publicBase}")
    private String publicBase;

    private final S3Client s3Client;

    public R2StorageServiceImpl(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFile(String key, InputStream inputStream, long size, String contentType) {
        try {
            // Use RequestBody.fromInputStream directly - more efficient for streams
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .contentLength(size) // Use the provided size
                    .build();

            // Direct upload without retry (let S3Client handle retries)
            s3Client.putObject(putRequest, RequestBody.fromInputStream(inputStream, size));

            return publicBase + "/" + key;

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to R2: " + e.getMessage(), e);
        }
    }

    public byte[] downloadFile(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
        return objectBytes.asByteArray();
    }

    public void deleteFile(String key) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }
}