package com.maxpri.repetgel.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class QuestionCreateDTO {
    @NotBlank
    private String questionText;

    @NotEmpty
    @Size(min = 2)
    private List<@Valid AnswerOptionCreateDTO> options;
}