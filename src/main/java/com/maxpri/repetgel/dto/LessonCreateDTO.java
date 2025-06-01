package com.maxpri.repetgel.dto;

import com.maxpri.repetgel.entity.LessonStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class LessonCreateDTO {
    @NotBlank
    @Size(max = 255)
    private String title;

    @Size(max = 5000)
    private String description;

    @NotNull
    private OffsetDateTime startTime;

    @NotNull
    private OffsetDateTime endTime;

    private boolean isRecurring = false;
    private String recurrenceRule;

    @NotNull
    private LessonStatus status = LessonStatus.SCHEDULED;
}