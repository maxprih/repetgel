package com.maxpri.repetgel.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class StudentTestAnswerDTO {
    private UUID questionId;
    private UUID chosenOptionId;
}