package com.maxpri.repetgel.controller;

import com.maxpri.repetgel.dto.HomeworkCreateDTO;
import com.maxpri.repetgel.dto.HomeworkDTO;
import com.maxpri.repetgel.dto.HomeworkDeadlineExtensionDTO;
import com.maxpri.repetgel.dto.HomeworkStudentViewDTO;
import com.maxpri.repetgel.dto.HomeworkSubmissionDTO;
import com.maxpri.repetgel.dto.HomeworkSubmissionGradeDTO;
import com.maxpri.repetgel.dto.HomeworkSubmissionRequestDTO;
import com.maxpri.repetgel.dto.HomeworkUpdateDTO;
import com.maxpri.repetgel.dto.PageDTO;
import com.maxpri.repetgel.dto.TestCreateDTO;
import com.maxpri.repetgel.dto.TestDTO;
import com.maxpri.repetgel.service.HomeworkService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/homework")
@RequiredArgsConstructor
public class HomeworkController {

    private final HomeworkService homeworkService;

    @PostMapping
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<HomeworkDTO> createHomework(@Valid @RequestBody HomeworkCreateDTO homeworkCreateDTO) {
        return new ResponseEntity<>(homeworkService.createHomework(homeworkCreateDTO), HttpStatus.CREATED);
    }

    @GetMapping("/{homeworkId}/tutor")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<HomeworkDTO> getHomeworkByIdForTutor(@PathVariable UUID homeworkId) {
        return ResponseEntity.ok(homeworkService.getHomeworkByIdForTutor(homeworkId));
    }

    @GetMapping("/{homeworkId}/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<HomeworkStudentViewDTO> getHomeworkByIdForStudent(@PathVariable UUID homeworkId) {
        return ResponseEntity.ok(homeworkService.getHomeworkByIdForStudent(homeworkId));
    }

    @GetMapping("/lesson/{lessonId}/tutor")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<PageDTO<HomeworkDTO>> getAllHomeworkByLessonForTutor(
            @PathVariable UUID lessonId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "deadline,asc") String[] sort) {
        Sort.Direction direction = sort[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
        return ResponseEntity.ok(homeworkService.getAllHomeworkByLessonForTutor(lessonId, pageable));
    }

    @GetMapping("/class/{classId}/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<PageDTO<HomeworkStudentViewDTO>> getAllHomeworkByClassForStudent(
            @PathVariable UUID classId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "deadline,asc") String[] sort) {
        Sort.Direction direction = sort[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
        return ResponseEntity.ok(homeworkService.getAllHomeworkByClassForStudent(classId, pageable));
    }
    
    @GetMapping("/student/my-homework")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<PageDTO<HomeworkStudentViewDTO>> getAllHomeworkForCurrentStudent(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "deadline,asc") String[] sort) {
        Sort.Direction direction = sort[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
        return ResponseEntity.ok(homeworkService.getAllHomeworkForCurrentStudent(pageable));
    }


    @PutMapping("/{homeworkId}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<HomeworkDTO> updateHomeworkDetails(
            @PathVariable UUID homeworkId,
            @Valid @RequestBody HomeworkUpdateDTO homeworkUpdateDTO) {
        return ResponseEntity.ok(homeworkService.updateHomeworkDetails(homeworkId, homeworkUpdateDTO));
    }

    @DeleteMapping("/{homeworkId}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<Void> deleteHomework(@PathVariable UUID homeworkId) {
        homeworkService.deleteHomework(homeworkId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{homeworkId}/test")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<TestDTO> getTestForHomework(@PathVariable UUID homeworkId) {
        return ResponseEntity.ok(homeworkService.getTestForHomework(homeworkId));
    }

    @PutMapping("/{homeworkId}/test")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<TestDTO> updateTestForHomework(
            @PathVariable UUID homeworkId,
            @Valid @RequestBody TestCreateDTO testCreateDTO) {
        return ResponseEntity.ok(homeworkService.updateTestForHomework(homeworkId, testCreateDTO));
    }

    @PostMapping("/{homeworkId}/submit/file")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<HomeworkSubmissionDTO> submitFileUploadHomework(
            @PathVariable UUID homeworkId,
            @RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>(homeworkService.submitFileUploadHomework(homeworkId, file), HttpStatus.CREATED);
    }

    @PostMapping("/{homeworkId}/submit/test")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<HomeworkSubmissionDTO> submitTestHomework(
            @PathVariable UUID homeworkId,
            @Valid @RequestBody HomeworkSubmissionRequestDTO submissionRequestDTO) {
        return new ResponseEntity<>(homeworkService.submitTestHomework(homeworkId, submissionRequestDTO), HttpStatus.CREATED);
    }

    @PostMapping("/submissions/{submissionId}/grade")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<HomeworkSubmissionDTO> gradeSubmission(
            @PathVariable UUID submissionId,
            @Valid @RequestBody HomeworkSubmissionGradeDTO gradeDTO) {
        return ResponseEntity.ok(homeworkService.gradeSubmission(submissionId, gradeDTO));
    }

    @GetMapping("/{homeworkId}/submissions/tutor")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<PageDTO<HomeworkSubmissionDTO>> getSubmissionsForHomeworkByTutor(
            @PathVariable UUID homeworkId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "submissionTime,desc") String[] sort) {
        Sort.Direction direction = sort[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
        return ResponseEntity.ok(homeworkService.getSubmissionsForHomeworkByTutor(homeworkId, pageable));
    }
    
    @GetMapping("/submissions/{submissionId}/tutor")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<HomeworkSubmissionDTO> getSubmissionByIdForTutor(@PathVariable UUID submissionId) {
        return ResponseEntity.ok(homeworkService.getSubmissionByIdForTutor(submissionId));
    }

    @GetMapping("/{homeworkId}/my-submission/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<HomeworkSubmissionDTO> getMySubmissionForHomework(@PathVariable UUID homeworkId) {
        return ResponseEntity.ok(homeworkService.getMySubmissionForHomework(homeworkId));
    }
    
    @PostMapping("/{homeworkId}/students/{studentId}/extend-deadline")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<Void> extendHomeworkDeadlineForStudent(
            @PathVariable UUID homeworkId,
            @PathVariable String studentId,
            @Valid @RequestBody HomeworkDeadlineExtensionDTO extensionDTO) {
        homeworkService.extendHomeworkDeadlineForStudent(homeworkId, studentId, extensionDTO);
        return ResponseEntity.ok().build();
    }
}