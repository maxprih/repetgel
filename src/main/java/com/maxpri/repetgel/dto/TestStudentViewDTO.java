package com.maxpri.repetgel.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class TestStudentViewDTO {
    private UUID id;
    private UUID homeworkId;
    private List<QuestionStudentViewDTO> questions;
}