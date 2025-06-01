package com.maxpri.repetgel.controller;

import com.maxpri.repetgel.dto.ClassCreateDTO;
import com.maxpri.repetgel.dto.ClassDetailsDTO;
import com.maxpri.repetgel.dto.ClassInfoDTO;
import com.maxpri.repetgel.dto.ClassUpdateDTO;
import com.maxpri.repetgel.dto.PageDTO;
import com.maxpri.repetgel.dto.StudentDTO;
import com.maxpri.repetgel.service.ClassService;
import com.maxpri.repetgel.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClassController {

    private final ClassService classService;

    @PostMapping
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<ClassDetailsDTO> createClass(@Valid @RequestBody ClassCreateDTO classCreateDTO) {
        return new ResponseEntity<>(classService.createClass(classCreateDTO), HttpStatus.CREATED);
    }

    @GetMapping("/{classId}")
    @PreAuthorize("hasAnyRole('TUTOR', 'STUDENT')")
    public ResponseEntity<ClassDetailsDTO> getClassById(@PathVariable UUID classId) {
        if (SecurityUtils.getCurrentUserKeycloakId().isPresent()) {
            return ResponseEntity.ok(classService.getClassByIdForTutor(classId));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    
    @GetMapping("/tutor/my-classes")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<PageDTO<ClassInfoDTO>> getMyClassesAsTutor(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {
        Sort.Direction direction = sort[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
        return ResponseEntity.ok(classService.getAllClassesForCurrentTutor(pageable));
    }

    @GetMapping("/student/my-classes")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<PageDTO<ClassInfoDTO>> getMyClassesAsStudent(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name,asc") String[] sort) {
        Sort.Direction direction = sort[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
        return ResponseEntity.ok(classService.getAllClassesForCurrentStudent(pageable));
    }

    @GetMapping("/{classId}/details/tutor")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<ClassDetailsDTO> getClassDetailsForTutor(@PathVariable UUID classId) {
        return ResponseEntity.ok(classService.getClassByIdForTutor(classId));
    }

    @GetMapping("/{classId}/details/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ClassDetailsDTO> getClassDetailsForStudent(@PathVariable UUID classId) {
        return ResponseEntity.ok(classService.getClassByIdForStudent(classId));
    }


    @PutMapping("/{classId}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<ClassDetailsDTO> updateClass(@PathVariable UUID classId, @Valid @RequestBody ClassUpdateDTO classUpdateDTO) {
        return ResponseEntity.ok(classService.updateClass(classId, classUpdateDTO));
    }

    @DeleteMapping("/{classId}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<Void> deleteClass(@PathVariable UUID classId) {
        classService.deleteClass(classId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{classId}/students")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<ClassDetailsDTO> addStudentToClass(@PathVariable UUID classId, @RequestParam String studentEmail) {
        return ResponseEntity.ok(classService.addStudentToClass(classId, studentEmail));
    }

    @DeleteMapping("/{classId}/students/{studentId}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<ClassDetailsDTO> removeStudentFromClass(@PathVariable UUID classId, @PathVariable String studentId) {
        return ResponseEntity.ok(classService.removeStudentFromClass(classId, studentId));
    }

    @GetMapping("/{classId}/students")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<List<StudentDTO>> getEnrolledStudents(@PathVariable UUID classId) {
        return ResponseEntity.ok(classService.getEnrolledStudents(classId));
    }
}