package com.maxpri.repetgel.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClassUpdateDTO {
    @Size(max = 255)
    private String name;

    @Size(max = 5000)
    private String description;
}