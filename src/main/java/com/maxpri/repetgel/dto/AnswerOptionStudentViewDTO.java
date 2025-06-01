package com.maxpri.repetgel.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class AnswerOptionStudentViewDTO {
    private UUID id;
    private String optionText;
}