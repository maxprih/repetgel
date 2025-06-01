package com.maxpri.repetgel.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class HomeworkSubmissionDTO {
    private UUID id;
    private UUID homeworkId;
    private String studentId;
    private String studentName;
    private OffsetDateTime submissionTime;
    private String submittedFileKey;
    private String submittedFileDownloadUrl;
    private String submittedFileName;
    private List<StudentTestAnswerDTO> testAnswers;
    private Integer grade;
    private String feedback;
    private boolean isLate;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}