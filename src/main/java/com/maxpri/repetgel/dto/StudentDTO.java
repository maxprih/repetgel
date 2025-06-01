package com.maxpri.repetgel.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StudentDTO extends UserDTO {
    private Integer livesRemaining;
}