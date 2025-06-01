package com.maxpri.repetgel.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HomeworkSubmissionGradeDTO {
    @NotNull
    @Min(0)
    @Max(100)
    private Integer grade;
    private String feedback;
}