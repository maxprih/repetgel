package com.maxpri.repetgel.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class TestDTO {
    private UUID id;
    private UUID homeworkId;
    private List<QuestionDTO> questions;
}