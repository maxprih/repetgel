package com.maxpri.repetgel.dto;

import com.maxpri.repetgel.entity.HomeworkType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class HomeworkCreateDTO {
    @NotNull
    private UUID lessonId;

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private HomeworkType type;

    @NotNull
    @FutureOrPresent
    private OffsetDateTime deadline;

    @Valid
    private TestCreateDTO test;
}