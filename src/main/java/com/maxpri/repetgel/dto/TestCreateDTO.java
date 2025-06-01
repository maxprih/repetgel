package com.maxpri.repetgel.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class TestCreateDTO {
    @NotEmpty
    private List<@Valid QuestionCreateDTO> questions;
}