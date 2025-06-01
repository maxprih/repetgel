package com.maxpri.repetgel.controller;

import com.maxpri.repetgel.dto.CalendarEventDTO;
import com.maxpri.repetgel.dto.LessonCreateDTO;
import com.maxpri.repetgel.dto.LessonDTO;
import com.maxpri.repetgel.dto.LessonFileAttachmentDTO;
import com.maxpri.repetgel.dto.LessonUpdateDTO;
import com.maxpri.repetgel.dto.PageDTO;
import com.maxpri.repetgel.service.LessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @PostMapping("/class/{classId}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<LessonDTO> createLesson(
            @PathVariable UUID classId,
            @Valid @RequestBody LessonCreateDTO lessonCreateDTO) {
        return new ResponseEntity<>(lessonService.createLesson(classId, lessonCreateDTO), HttpStatus.CREATED);
    }
    
    @GetMapping("/{lessonId}/tutor")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<LessonDTO> getLessonByIdForTutor(@PathVariable UUID lessonId) {
        return ResponseEntity.ok(lessonService.getLessonByIdForTutor(lessonId));
    }

    @GetMapping("/{lessonId}/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<LessonDTO> getLessonByIdForStudent(@PathVariable UUID lessonId) {
        return ResponseEntity.ok(lessonService.getLessonByIdForStudent(lessonId));
    }

    @GetMapping("/class/{classId}/tutor")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<PageDTO<LessonDTO>> getAllLessonsByClassForTutor(
            @PathVariable UUID classId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startTime,asc") String[] sort) {
        Sort.Direction direction = sort[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
        return ResponseEntity.ok(lessonService.getAllLessonsByClassForTutor(classId, pageable));
    }

    @GetMapping("/class/{classId}/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<PageDTO<LessonDTO>> getAllLessonsByClassForStudent(
            @PathVariable UUID classId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startTime,asc") String[] sort) {
        Sort.Direction direction = sort[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
        return ResponseEntity.ok(lessonService.getAllLessonsByClassForStudent(classId, pageable));
    }

    @PutMapping("/{lessonId}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<LessonDTO> updateLesson(
            @PathVariable UUID lessonId,
            @Valid @RequestBody LessonUpdateDTO lessonUpdateDTO) {
        return ResponseEntity.ok(lessonService.updateLesson(lessonId, lessonUpdateDTO));
    }

    @DeleteMapping("/{lessonId}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<Void> deleteLesson(@PathVariable UUID lessonId) {
        lessonService.deleteLesson(lessonId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{lessonId}/attachments")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<LessonFileAttachmentDTO> addAttachmentToLesson(
            @PathVariable UUID lessonId,
            @RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>(lessonService.addAttachmentToLesson(lessonId, file), HttpStatus.CREATED);
    }

    @DeleteMapping("/{lessonId}/attachments/{attachmentId}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<Void> deleteAttachmentFromLesson(
            @PathVariable UUID lessonId,
            @PathVariable UUID attachmentId) {
        lessonService.deleteAttachmentFromLesson(lessonId, attachmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{lessonId}/attachments")
    @PreAuthorize("hasAnyRole('TUTOR', 'STUDENT')")
    public ResponseEntity<List<LessonFileAttachmentDTO>> getLessonAttachments(@PathVariable UUID lessonId) {
        return ResponseEntity.ok(lessonService.getLessonAttachments(lessonId));
    }

    @GetMapping("/calendar/tutor")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<List<CalendarEventDTO>> getTutorCalendarEvents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end) {
        return ResponseEntity.ok(lessonService.getTutorCalendarEvents(start, end));
    }

    @GetMapping("/calendar/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<CalendarEventDTO>> getStudentCalendarEvents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end) {
        return ResponseEntity.ok(lessonService.getStudentCalendarEvents(start, end));
    }
}