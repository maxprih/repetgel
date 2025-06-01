package com.maxpri.repetgel.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentTestAnswer {
    private UUID questionId;
    private UUID chosenOptionId;
}