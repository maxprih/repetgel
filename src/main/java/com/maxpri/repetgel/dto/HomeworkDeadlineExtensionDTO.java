package com.maxpri.repetgel.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class HomeworkDeadlineExtensionDTO {
    @NotNull
    @Future
    private OffsetDateTime newDeadline;
}