package com.maxpri.repetgel.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class ClassDetailsDTO extends ClassInfoDTO {
    private Set<StudentDTO> students;
}