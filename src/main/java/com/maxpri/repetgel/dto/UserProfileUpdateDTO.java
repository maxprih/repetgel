package com.maxpri.repetgel.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileUpdateDTO {
    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;
}