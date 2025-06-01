package com.maxpri.repetgel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventDTO {
    private UUID eventId;
    private String title;
    private OffsetDateTime start;
    private OffsetDateTime end;
    private String type;
    private String description;
    private String color;
}