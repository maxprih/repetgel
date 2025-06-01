package com.maxpri.repetgel.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class QuestionStudentViewDTO {
    private UUID id;
    private String questionText;
    private List<AnswerOptionStudentViewDTO> options;
}