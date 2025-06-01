package com.maxpri.repetgel.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FileStorageService {
    String uploadFile(MultipartFile file, String pathPrefix);
    String uploadInputStream(InputStream inputStream, long size, String contentType, String originalFilename, String pathPrefix);
    byte[] downloadFile(String fileKey);
    String getPresignedUrl(String fileKey, int durationMinutes);
    void deleteFile(String fileKey);
    boolean fileExists(String fileKey);
}