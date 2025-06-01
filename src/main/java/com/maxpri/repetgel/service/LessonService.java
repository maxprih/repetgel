package com.maxpri.repetgel.service;

import com.maxpri.repetgel.dto.CalendarEventDTO;
import com.maxpri.repetgel.dto.LessonCreateDTO;
import com.maxpri.repetgel.dto.LessonDTO;
import com.maxpri.repetgel.dto.LessonFileAttachmentDTO;
import com.maxpri.repetgel.dto.LessonUpdateDTO;
import com.maxpri.repetgel.dto.PageDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface LessonService {
    LessonDTO createLesson(UUID classId, LessonCreateDTO lessonCreateDTO);

    LessonDTO getLessonByIdForTutor(UUID lessonId);

    LessonDTO getLessonByIdForStudent(UUID lessonId);

    PageDTO<LessonDTO> getAllLessonsByClassForTutor(UUID classId, Pageable pageable);

    PageDTO<LessonDTO> getAllLessonsByClassForStudent(UUID classId, Pageable pageable);

    LessonDTO updateLesson(UUID lessonId, LessonUpdateDTO lessonUpdateDTO);

    void deleteLesson(UUID lessonId);

    LessonFileAttachmentDTO addAttachmentToLesson(UUID lessonId, MultipartFile file);

    void deleteAttachmentFromLesson(UUID lessonId, UUID attachmentId);

    List<LessonFileAttachmentDTO> getLessonAttachments(UUID lessonId);

    List<CalendarEventDTO> getTutorCalendarEvents(OffsetDateTime start, OffsetDateTime end);

    List<CalendarEventDTO> getStudentCalendarEvents(OffsetDateTime start, OffsetDateTime end);
}