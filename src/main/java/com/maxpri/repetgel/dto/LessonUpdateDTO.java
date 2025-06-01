package com.maxpri.repetgel.dto;

import com.maxpri.repetgel.entity.LessonStatus;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class LessonUpdateDTO {
    private String title;
    private String description;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private Boolean isRecurring;
    private String recurrenceRule;
    private LessonStatus status;
}