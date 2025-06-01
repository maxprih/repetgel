package com.maxpri.repetgel.dto;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class HomeworkSubmissionRequestDTO {

    private List<@Valid StudentTestAnswerDTO> testAnswers;
}