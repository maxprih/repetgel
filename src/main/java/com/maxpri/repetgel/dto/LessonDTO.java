package com.maxpri.repetgel.dto;

import com.maxpri.repetgel.entity.LessonStatus;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@Data
public class LessonDTO {
    private UUID id;
    private UUID classId;
    private String title;
    private String description;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private boolean isRecurring;
    private String recurrenceRule;
    private LessonStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Set<LessonFileAttachmentDTO> attachedFiles;
}