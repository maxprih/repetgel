package com.maxpri.repetgel.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class AnswerOptionDTO {
    private UUID id;
    private String optionText;
    private Boolean isCorrect;
}