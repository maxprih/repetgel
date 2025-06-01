package com.maxpri.repetgel.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class LessonFileAttachmentDTO {
    private UUID id;
    private String fileName;
    private String s3FileKey;
    private String fileType;
    private OffsetDateTime uploadedAt;
    private String downloadUrl;
}