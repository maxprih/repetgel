package com.maxpri.repetgel.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class QuestionDTO {
    private UUID id;
    private String questionText;
    private List<AnswerOptionDTO> options;
}