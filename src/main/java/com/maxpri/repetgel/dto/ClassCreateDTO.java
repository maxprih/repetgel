package com.maxpri.repetgel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClassCreateDTO {
    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 5000)
    private String description;
}