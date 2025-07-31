package com.projectmanager.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;
import java.time.Duration;

@Configuration
public class R2Config {

    @Value("${cloudflare.r2.accessKey}")
    private String accessKey;

    @Value("${cloudflare.r2.secretKey}")
    private String secretKey;

    @Value("${cloudflare.r2.endpoint}")
    private String endpoint;

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    @Bean
    public S3Client s3Client() {

        if (accessKey == null || secretKey == null || endpoint == null) {
            throw new IllegalStateException("R2 credentials are not properly configured");
        }
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of("auto")) // Use AUTO for Cloudflare R2
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true) // Critical for R2
                        .checksumValidationEnabled(false) // Disable checksum validation for R2
                        .chunkedEncodingEnabled(false) // Disable chunked encoding
                        .build())
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .apiCallTimeout(Duration.ofMinutes(5))
                        .apiCallAttemptTimeout(Duration.ofMinutes(2))
                        .retryPolicy(policy -> policy.numRetries(3))
                        .build())
                .build();
    }
}