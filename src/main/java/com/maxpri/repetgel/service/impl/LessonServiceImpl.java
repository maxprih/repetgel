package com.maxpri.repetgel.service.impl;

import com.maxpri.repetgel.config.AppProperties;
import com.maxpri.repetgel.dto.CalendarEventDTO;
import com.maxpri.repetgel.dto.LessonCreateDTO;
import com.maxpri.repetgel.dto.LessonDTO;
import com.maxpri.repetgel.dto.LessonFileAttachmentDTO;
import com.maxpri.repetgel.dto.LessonUpdateDTO;
import com.maxpri.repetgel.dto.PageDTO;
import com.maxpri.repetgel.entity.ClassEntity;
import com.maxpri.repetgel.entity.Lesson;
import com.maxpri.repetgel.entity.LessonFileAttachment;
import com.maxpri.repetgel.entity.Student;
import com.maxpri.repetgel.entity.Tutor;
import com.maxpri.repetgel.exception.BadRequestException;
import com.maxpri.repetgel.exception.ResourceNotFoundException;
import com.maxpri.repetgel.exception.UnauthorizedOperationException;
import com.maxpri.repetgel.mapper.LessonMapper;
import com.maxpri.repetgel.repository.ClassEntityRepository;
import com.maxpri.repetgel.repository.LessonFileAttachmentRepository;
import com.maxpri.repetgel.repository.LessonRepository;
import com.maxpri.repetgel.service.FileStorageService;
import com.maxpri.repetgel.service.LessonService;
import com.maxpri.repetgel.service.UserService;
import com.maxpri.repetgel.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final ClassEntityRepository classRepository;
    private final LessonFileAttachmentRepository attachmentRepository;
    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final LessonMapper lessonMapper;
    private final SecurityUtils securityUtils;
    private final AppProperties appProperties;

    private static final String LESSON_DETAILS_CACHE = "lessonDetails";
    private static final String LESSONS_BY_CLASS_CACHE = "lessonsByClass";
    private static final String LESSON_ATTACHMENTS_CACHE = "lessonAttachments";


    private ClassEntity getClassForTutor(UUID classId, String tutorId) {
        return classRepository.findByIdAndTutorKeycloakUserId(classId, tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Class " + classId + " not found for tutor " + tutorId));
    }
    
    private ClassEntity getClassForStudent(UUID classId, String studentId) {
        ClassEntity classEntity = classRepository.findById(classId)
            .orElseThrow(() -> new ResourceNotFoundException("Class", "ID", classId));
        if (!classRepository.isStudentEnrolled(classId, studentId)) {
            throw new UnauthorizedOperationException("Student " + studentId + " is not enrolled in class " + classId);
        }
        return classEntity;
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = LESSONS_BY_CLASS_CACHE, key = "#classId + '*'")
    })
    public LessonDTO createLesson(UUID classId, LessonCreateDTO lessonCreateDTO) {
        Tutor currentTutor = userService.getCurrentTutorEntity();
        ClassEntity classEntity = getClassForTutor(classId, currentTutor.getKeycloakUserId());

        if (lessonCreateDTO.getEndTime().isBefore(lessonCreateDTO.getStartTime())) {
            throw new BadRequestException("Lesson end time cannot be before start time.");
        }

        Lesson lesson = lessonMapper.toEntity(lessonCreateDTO);
        lesson.setClassEntity(classEntity);
        Lesson savedLesson = lessonRepository.save(lesson);
        log.info("Tutor {} created lesson {} (ID: {}) for class {}", currentTutor.getKeycloakUserId(), savedLesson.getTitle(), savedLesson.getId(), classId);
        return lessonMapper.toLessonDTO(savedLesson);
    }

    private LessonDTO enrichLessonDTOWithAttachmentUrls(LessonDTO lessonDTO) {
        if (lessonDTO != null && lessonDTO.getAttachedFiles() != null) {
            lessonDTO.getAttachedFiles().forEach(att -> {
                if (att.getS3FileKey() != null) {
                    att.setDownloadUrl(fileStorageService.getPresignedUrl(att.getS3FileKey(), appProperties.getFiles().getPresignedUrlDurationMinutes()));
                }
            });
        }
        return lessonDTO;
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = LESSON_DETAILS_CACHE, key = "#lessonId + '_tutor'")
    public LessonDTO getLessonByIdForTutor(UUID lessonId) {
        String tutorId = securityUtils.getCurrentUserKeycloakIdRequired();
        Lesson lesson = lessonRepository.findByIdAndClassEntityTutorKeycloakUserId(lessonId, tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id " + lessonId + " for tutor", tutorId));
        return enrichLessonDTOWithAttachmentUrls(lessonMapper.toLessonDTO(lesson));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = LESSON_DETAILS_CACHE, key = "#lessonId + '_student'")
    public LessonDTO getLessonByIdForStudent(UUID lessonId) {
        String studentId = securityUtils.getCurrentUserKeycloakIdRequired();
         Lesson lesson = lessonRepository.findByIdAndClassEntityStudentsKeycloakUserId(lessonId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id " + lessonId + " for student", studentId));
        return enrichLessonDTOWithAttachmentUrls(lessonMapper.toLessonDTO(lesson));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = LESSONS_BY_CLASS_CACHE, key = "#classId + '_tutor_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public PageDTO<LessonDTO> getAllLessonsByClassForTutor(UUID classId, Pageable pageable) {
        Tutor currentTutor = userService.getCurrentTutorEntity();
        getClassForTutor(classId, currentTutor.getKeycloakUserId());

        Page<Lesson> lessonPage = lessonRepository.findAllByClassEntityId(classId, pageable);
        List<LessonDTO> lessonDTOs = lessonPage.getContent().stream()
                .map(lessonMapper::toLessonDTO)
                .map(this::enrichLessonDTOWithAttachmentUrls)
                .collect(Collectors.toList());
        return new PageDTO<>(lessonDTOs, lessonPage.getNumber(), lessonPage.getSize(), lessonPage.getTotalElements(), lessonPage.getTotalPages(), lessonPage.isLast());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = LESSONS_BY_CLASS_CACHE, key = "#classId + '_student_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public PageDTO<LessonDTO> getAllLessonsByClassForStudent(UUID classId, Pageable pageable) {
        Student currentStudent = userService.getCurrentStudentEntity();
        getClassForStudent(classId, currentStudent.getKeycloakUserId());

        Page<Lesson> lessonPage = lessonRepository.findAllByClassEntityId(classId, pageable);
        List<LessonDTO> lessonDTOs = lessonPage.getContent().stream()
                .map(lessonMapper::toLessonDTO)
                .map(this::enrichLessonDTOWithAttachmentUrls)
                .collect(Collectors.toList());
        return new PageDTO<>(lessonDTOs, lessonPage.getNumber(), lessonPage.getSize(), lessonPage.getTotalElements(), lessonPage.getTotalPages(), lessonPage.isLast());
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = LESSON_DETAILS_CACHE, key = "#lessonId + '*'"),
            @CacheEvict(value = LESSONS_BY_CLASS_CACHE, allEntries = true)
    })
    public LessonDTO updateLesson(UUID lessonId, LessonUpdateDTO lessonUpdateDTO) {
        Tutor currentTutor = userService.getCurrentTutorEntity();
        Lesson lesson = lessonRepository.findByIdAndClassEntityTutorKeycloakUserId(lessonId, currentTutor.getKeycloakUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found or not authorized. ID: " + lessonId));

        lessonMapper.updateEntityFromDto(lessonUpdateDTO, lesson);
        if (lesson.getEndTime().isBefore(lesson.getStartTime())) {
            throw new BadRequestException("Lesson end time cannot be before start time.");
        }
        Lesson updatedLesson = lessonRepository.save(lesson);
        log.info("Tutor {} updated lesson {} (ID: {})", currentTutor.getKeycloakUserId(), updatedLesson.getTitle(), updatedLesson.getId());
        return enrichLessonDTOWithAttachmentUrls(lessonMapper.toLessonDTO(updatedLesson));
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = LESSON_DETAILS_CACHE, key = "#lessonId + '*'"),
            @CacheEvict(value = LESSONS_BY_CLASS_CACHE, allEntries = true),
            @CacheEvict(value = LESSON_ATTACHMENTS_CACHE, key = "#lessonId")
    })
    public void deleteLesson(UUID lessonId) {
        Tutor currentTutor = userService.getCurrentTutorEntity();
        Lesson lesson = lessonRepository.findByIdAndClassEntityTutorKeycloakUserId(lessonId, currentTutor.getKeycloakUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found or not authorized. ID: " + lessonId));
        lesson.getAttachedFiles().forEach(attachment -> {
            try {
                fileStorageService.deleteFile(attachment.getS3FileKey());
            } catch (Exception e) {
                log.error("Failed to delete lesson attachment {} from MinIO for lesson {}: {}", attachment.getS3FileKey(), lessonId, e.getMessage());
            }
        });

        lessonRepository.delete(lesson);
        log.info("Tutor {} deleted lesson with ID {}", currentTutor.getKeycloakUserId(), lessonId);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = LESSON_DETAILS_CACHE, key = "#lessonId + '*'"),
        @CacheEvict(value = LESSON_ATTACHMENTS_CACHE, key = "#lessonId")
    })
    public LessonFileAttachmentDTO addAttachmentToLesson(UUID lessonId, MultipartFile file) {
        Tutor currentTutor = userService.getCurrentTutorEntity();
        Lesson lesson = lessonRepository.findByIdAndClassEntityTutorKeycloakUserId(lessonId, currentTutor.getKeycloakUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found or not authorized. ID: " + lessonId));

        String pathPrefix = "lessons/" + lesson.getId().toString() + "/attachments";
        String s3FileKey = fileStorageService.uploadFile(file, pathPrefix);

        LessonFileAttachment attachment = new LessonFileAttachment();
        attachment.setLesson(lesson);
        attachment.setFileName(file.getOriginalFilename());
        attachment.setS3FileKey(s3FileKey);
        attachment.setFileType(file.getContentType());

        LessonFileAttachment savedAttachment = attachmentRepository.save(attachment);
        log.info("Tutor {} added attachment {} (S3 key: {}) to lesson {}", currentTutor.getKeycloakUserId(), savedAttachment.getFileName(), s3FileKey, lessonId);

        LessonFileAttachmentDTO dto = lessonMapper.toLessonFileAttachmentDTO(savedAttachment);
        dto.setDownloadUrl(fileStorageService.getPresignedUrl(s3FileKey, appProperties.getFiles().getPresignedUrlDurationMinutes()));
        return dto;
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = LESSON_DETAILS_CACHE, key = "#lessonId + '*'"),
        @CacheEvict(value = LESSON_ATTACHMENTS_CACHE, key = "#lessonId")
    })
    public void deleteAttachmentFromLesson(UUID lessonId, UUID attachmentId) {
        Tutor currentTutor = userService.getCurrentTutorEntity();
        Lesson lesson = lessonRepository.findByIdAndClassEntityTutorKeycloakUserId(lessonId, currentTutor.getKeycloakUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found or not authorized. ID: " + lessonId));
        
        LessonFileAttachment attachment = attachmentRepository.findByIdAndLessonId(attachmentId, lessonId)
            .orElseThrow(() -> new ResourceNotFoundException("Attachment", "ID", attachmentId));

        try {
            fileStorageService.deleteFile(attachment.getS3FileKey());
        } catch (Exception e) {
            log.error("Failed to delete attachment file {} from MinIO for lesson {}: {}", attachment.getS3FileKey(), lessonId, e.getMessage());
            throw new BadRequestException("Failed to delete file from storage. DB record not deleted.");
        }
        
        attachmentRepository.delete(attachment);
        log.info("Tutor {} deleted attachment {} (ID: {}) from lesson {}", currentTutor.getKeycloakUserId(), attachment.getFileName(), attachmentId, lessonId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = LESSON_ATTACHMENTS_CACHE, key = "#lessonId")
    public List<LessonFileAttachmentDTO> getLessonAttachments(UUID lessonId) {
        String currentUserId = securityUtils.getCurrentUserKeycloakIdRequired();
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "ID", lessonId));

        boolean isTutor = lesson.getClassEntity().getTutor().getKeycloakUserId().equals(currentUserId);
        boolean isStudentInClass = lesson.getClassEntity().getStudents().stream()
                .anyMatch(s -> s.getKeycloakUserId().equals(currentUserId));

        if (!isTutor && !isStudentInClass) {
            throw new UnauthorizedOperationException("User " + currentUserId + " is not authorized to view attachments for lesson " + lessonId);
        }

        return attachmentRepository.findAllByLessonId(lessonId).stream()
                .map(attachment -> {
                    LessonFileAttachmentDTO dto = lessonMapper.toLessonFileAttachmentDTO(attachment);
                    dto.setDownloadUrl(fileStorageService.getPresignedUrl(attachment.getS3FileKey(), appProperties.getFiles().getPresignedUrlDurationMinutes()));
                    return dto;
                })
                .collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public List<CalendarEventDTO> getTutorCalendarEvents(OffsetDateTime start, OffsetDateTime end) {
        String tutorId = securityUtils.getCurrentUserKeycloakIdRequired();
        List<Lesson> lessons = lessonRepository.findAllByTutorAndDateRange(tutorId, start, end);
        return lessons.stream()
                .map(lesson -> new CalendarEventDTO(
                        lesson.getId(),
                        lesson.getTitle() + " (" + lesson.getClassEntity().getName() + ")",
                        lesson.getStartTime(),
                        lesson.getEndTime(),
                        "LESSON",
                        "Class: " + lesson.getClassEntity().getName(),
                        determineEventColor(lesson)
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarEventDTO> getStudentCalendarEvents(OffsetDateTime start, OffsetDateTime end) {
        String studentId = securityUtils.getCurrentUserKeycloakIdRequired();
        List<Lesson> lessons = lessonRepository.findAllByStudentAndDateRange(studentId, start, end);
        return lessons.stream()
                .map(lesson -> new CalendarEventDTO(
                        lesson.getId(),
                        lesson.getTitle(),
                        lesson.getStartTime(),
                        lesson.getEndTime(),
                        "LESSON",
                        "Class: " + lesson.getClassEntity().getName() + ", Tutor: " + lesson.getClassEntity().getTutor().getFirstName(),
                        determineEventColor(lesson)
                ))
                .collect(Collectors.toList());
    }

    private String determineEventColor(Lesson lesson) {
        return switch (lesson.getStatus()) {
            case SCHEDULED -> "#3788d8";
            case COMPLETED -> "#00a000";
            case CANCELLED -> "#d3d3d3";
            default -> "#808080";
        };
    }
}