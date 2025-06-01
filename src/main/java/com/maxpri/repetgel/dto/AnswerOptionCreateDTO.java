package com.maxpri.repetgel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnswerOptionCreateDTO {
    @NotBlank
    private String optionText;
    @NotNull
    private Boolean isCorrect;
}