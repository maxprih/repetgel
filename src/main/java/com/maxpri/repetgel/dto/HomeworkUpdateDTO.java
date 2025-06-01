package com.maxpri.repetgel.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class HomeworkUpdateDTO {
    private String title;
    private String description;
    private OffsetDateTime deadline;
}