package com.maxpri.repetgel.dto;

import com.maxpri.repetgel.entity.HomeworkType;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class HomeworkDTO {
    private UUID id;
    private UUID lessonId;
    private String title;
    private String description;
    private HomeworkType type;
    private OffsetDateTime deadline;
    private TestDTO test;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}