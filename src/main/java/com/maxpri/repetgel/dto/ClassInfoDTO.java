package com.maxpri.repetgel.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ClassInfoDTO {
    private UUID id;
    private String name;
    private String description;
    private UUID tutorId;
    private String tutorName;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private long studentCount;
}