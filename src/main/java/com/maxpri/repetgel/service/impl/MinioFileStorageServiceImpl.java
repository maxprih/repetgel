package com.maxpri.repetgel.service.impl;

import com.maxpri.repetgel.config.MinioConfig;
import com.maxpri.repetgel.exception.FileStorageException;
import com.maxpri.repetgel.service.FileStorageService;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioFileStorageServiceImpl implements FileStorageService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    @PostConstruct
    private void initBucket() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioConfig.getBucketName()).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioConfig.getBucketName()).build());
                log.info("MinIO bucket '{}' created successfully.", minioConfig.getBucketName());
            } else {
                log.info("MinIO bucket '{}' already exists.", minioConfig.getBucketName());
            }
        } catch (Exception e) {
            log.error("Could not initialize MinIO bucket: {}", e.getMessage(), e);
            throw new FileStorageException("Could not initialize MinIO bucket: " + e.getMessage(), e);
        }
    }

    private String generateFileKey(String pathPrefix, String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String prefix = (pathPrefix == null || pathPrefix.isEmpty()) ? "" : (pathPrefix.endsWith("/") ? pathPrefix : pathPrefix + "/");
        return prefix + UUID.randomUUID().toString() + extension;
    }

    @Override
    public String uploadFile(MultipartFile file, String pathPrefix) {
        try (InputStream inputStream = file.getInputStream()) {
            return uploadInputStream(inputStream, file.getSize(), file.getContentType(), file.getOriginalFilename(), pathPrefix);
        } catch (Exception e) {
            log.error("Failed to upload file to MinIO: {}", file.getOriginalFilename(), e);
            throw new FileStorageException("Failed to upload file: " + file.getOriginalFilename() + ". Error: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String uploadInputStream(InputStream inputStream, long size, String contentType, String originalFilename, String pathPrefix) {
        String fileKey = generateFileKey(pathPrefix, originalFilename);
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(fileKey)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build());
            log.info("File '{}' uploaded successfully to MinIO with key '{}'", originalFilename, fileKey);
            return fileKey;
        } catch (Exception e) {
            log.error("Failed to upload input stream to MinIO for original filename '{}': {}", originalFilename, e.getMessage(), e);
            throw new FileStorageException("Failed to upload file: " + originalFilename + ". Error: " + e.getMessage(), e);
        }
    }


    @Override
    public byte[] downloadFile(String fileKey) {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .object(fileKey)
                        .build())) {
            return stream.readAllBytes();
        } catch (Exception e) {
            log.error("Failed to download file from MinIO with key '{}': {}", fileKey, e.getMessage(), e);
            throw new FileStorageException("Failed to download file: " + fileKey + ". Error: " + e.getMessage(), e);
        }
    }

    @Override
    public String getPresignedUrl(String fileKey, int durationMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioConfig.getBucketName())
                            .object(fileKey)
                            .expiry(durationMinutes, TimeUnit.MINUTES)
                            .build());
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for file key '{}': {}", fileKey, e.getMessage(), e);
            throw new FileStorageException("Failed to generate presigned URL for file: " + fileKey, e);
        }
    }

    @Override
    public void deleteFile(String fileKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(fileKey)
                            .build());
            log.info("File '{}' deleted successfully from MinIO.", fileKey);
        } catch (Exception e) {
            log.error("Failed to delete file from MinIO with key '{}': {}", fileKey, e.getMessage(), e);
            throw new FileStorageException("Failed to delete file: " + fileKey + ". Error: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean fileExists(String fileKey) {
        try {
            minioClient.statObject(StatObjectArgs.builder().bucket(minioConfig.getBucketName()).object(fileKey).build());
            return true;
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                return false;
            }
            log.error("Error checking if file exists in MinIO for key '{}': {}", fileKey, e.getMessage(), e);
            throw new FileStorageException("Error checking file existence: " + fileKey, e);
        } catch (Exception e) {
            log.error("Error checking if file exists in MinIO for key '{}': {}", fileKey, e.getMessage(), e);
            throw new FileStorageException("Error checking file existence: " + fileKey, e);
        }
    }
}