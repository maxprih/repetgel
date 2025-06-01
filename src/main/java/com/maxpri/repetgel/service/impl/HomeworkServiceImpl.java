package com.maxpri.repetgel.service.impl;

import com.maxpri.repetgel.config.AppProperties;
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
import com.maxpri.repetgel.entity.AnswerOption;
import com.maxpri.repetgel.entity.ClassEntity;
import com.maxpri.repetgel.entity.Homework;
import com.maxpri.repetgel.entity.HomeworkSubmission;
import com.maxpri.repetgel.entity.HomeworkType;
import com.maxpri.repetgel.entity.Lesson;
import com.maxpri.repetgel.entity.Question;
import com.maxpri.repetgel.entity.Student;
import com.maxpri.repetgel.entity.StudentTestAnswer;
import com.maxpri.repetgel.entity.Test;
import com.maxpri.repetgel.entity.Tutor;
import com.maxpri.repetgel.exception.BadRequestException;
import com.maxpri.repetgel.exception.ResourceNotFoundException;
import com.maxpri.repetgel.exception.UnauthorizedOperationException;
import com.maxpri.repetgel.mapper.HomeworkMapper;
import com.maxpri.repetgel.mapper.HomeworkSubmissionMapper;
import com.maxpri.repetgel.mapper.TestMapper;
import com.maxpri.repetgel.repository.AnswerOptionRepository;
import com.maxpri.repetgel.repository.HomeworkRepository;
import com.maxpri.repetgel.repository.HomeworkSubmissionRepository;
import com.maxpri.repetgel.repository.LessonRepository;
import com.maxpri.repetgel.repository.QuestionRepository;
import com.maxpri.repetgel.repository.StudentRepository;
import com.maxpri.repetgel.repository.TestRepository;
import com.maxpri.repetgel.service.FileStorageService;
import com.maxpri.repetgel.service.HomeworkService;
import com.maxpri.repetgel.service.UserService;
import com.maxpri.repetgel.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeworkServiceImpl implements HomeworkService {

    private final HomeworkRepository homeworkRepository;
    private final LessonRepository lessonRepository;
    private final TestRepository testRepository;
    private final QuestionRepository questionRepository;
    private final AnswerOptionRepository answerOptionRepository;
    private final HomeworkSubmissionRepository submissionRepository;
    private final StudentRepository studentRepository;

    private final UserService userService;
    private final FileStorageService fileStorageService;

    private final HomeworkMapper homeworkMapper;
    private final TestMapper testMapper;
    private final HomeworkSubmissionMapper submissionMapper;

    private final SecurityUtils securityUtils;
    private final AppProperties appProperties;

    private static final String HOMEWORK_DETAILS_CACHE = "homeworkDetails";
    private static final String HOMEWORK_STUDENT_VIEW_CACHE = "homeworkStudentView";
    private static final String HOMEWORK_BY_LESSON_CACHE = "homeworkByLesson";
    private static final String HOMEWORK_BY_CLASS_STUDENT_CACHE = "homeworkByClassStudent";
    private static final String HOMEWORK_FOR_STUDENT_CACHE = "homeworkForStudent";
    private static final String TEST_DETAILS_CACHE = "testDetails";
    private static final String SUBMISSIONS_BY_HOMEWORK_CACHE = "submissionsByHomework";
    private static final String SUBMISSION_DETAILS_CACHE = "submissionDetails";


    private Lesson getLessonForTutor(UUID lessonId, String tutorId) {
        return lessonRepository.findByIdAndClassEntityTutorKeycloakUserId(lessonId, tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson " + lessonId + " not found for tutor " + tutorId));
    }
    
    private Homework getHomeworkEntityForTutor(UUID homeworkId, String tutorId) {
        return homeworkRepository.findByIdForTutor(homeworkId, tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Homework " + homeworkId + " not found for tutor " + tutorId));
    }

    private Homework getHomeworkEntityForStudent(UUID homeworkId, String studentId) {
        Homework homework = homeworkRepository.findById(homeworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Homework", "ID", homeworkId));
        boolean isStudentInClass = homework.getLesson().getClassEntity().getStudents().stream()
                .anyMatch(s -> s.getKeycloakUserId().equals(studentId));
        if (!isStudentInClass) {
            throw new UnauthorizedOperationException("Student " + studentId + " is not authorized to access homework " + homeworkId);
        }
        return homework;
    }


    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = HOMEWORK_BY_LESSON_CACHE, allEntries = true),
            @CacheEvict(value = HOMEWORK_BY_CLASS_STUDENT_CACHE, allEntries = true),
            @CacheEvict(value = HOMEWORK_FOR_STUDENT_CACHE, allEntries = true)
    })
    public HomeworkDTO createHomework(HomeworkCreateDTO homeworkCreateDTO) {
        Tutor currentTutor = userService.getCurrentTutorEntity();
        Lesson lesson = getLessonForTutor(homeworkCreateDTO.getLessonId(), currentTutor.getKeycloakUserId());

        Homework homework = homeworkMapper.toEntity(homeworkCreateDTO);
        homework.setLesson(lesson);

        if (homework.getType() == HomeworkType.TEST) {
            if (homeworkCreateDTO.getTest() == null) {
                throw new BadRequestException("Test details must be provided for homework of type TEST.");
            }
            Test testEntity = testMapper.toEntity(homeworkCreateDTO.getTest());
            testEntity.setHomework(homework);
            for (Question question : testEntity.getQuestions()) {
                question.setTest(testEntity);
                for (AnswerOption option : question.getOptions()) {
                    option.setQuestion(question);
                }
            }
            homework.setTest(testEntity);
        } else {
            homework.setTest(null);
        }

        Homework savedHomework = homeworkRepository.save(homework);
        log.info("Tutor {} created homework {} (ID: {}) for lesson {}", currentTutor.getKeycloakUserId(), savedHomework.getTitle(), savedHomework.getId(), lesson.getId());
        return homeworkMapper.toHomeworkDTO(savedHomework);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = HOMEWORK_DETAILS_CACHE, key = "#homeworkId + '_tutor'")
    public HomeworkDTO getHomeworkByIdForTutor(UUID homeworkId) {
        String tutorId = securityUtils.getCurrentUserKeycloakIdRequired();
        Homework homework = getHomeworkEntityForTutor(homeworkId, tutorId);
        return homeworkMapper.toHomeworkDTO(homework);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = HOMEWORK_STUDENT_VIEW_CACHE, key = "#homeworkId + '_' + T(com.maxpri.repetgel.utils.SecurityUtils).currentUserKeycloakId.orElse('anonymous')")
    public HomeworkStudentViewDTO getHomeworkByIdForStudent(UUID homeworkId) {
        String studentId = securityUtils.getCurrentUserKeycloakIdRequired();
        Homework homework = getHomeworkEntityForStudent(homeworkId, studentId);
        
        HomeworkStudentViewDTO dto = homeworkMapper.toHomeworkStudentViewDTO(homework);
        if (homework.getTest() != null) {
            dto.setTest(testMapper.toTestStudentViewDTO(homework.getTest()));
        }
        submissionRepository.findByHomeworkIdAndStudentKeycloakUserId(homeworkId, studentId)
            .ifPresent(submission -> dto.setMySubmission(submissionMapper.toHomeworkSubmissionDTO(submission)));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = HOMEWORK_BY_LESSON_CACHE, key = "#lessonId + '_tutor_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public PageDTO<HomeworkDTO> getAllHomeworkByLessonForTutor(UUID lessonId, Pageable pageable) {
        String tutorId = securityUtils.getCurrentUserKeycloakIdRequired();
        getLessonForTutor(lessonId, tutorId);

        Page<Homework> homeworkPage = homeworkRepository.findAllByLessonId(lessonId, pageable);
        List<HomeworkDTO> dtos = homeworkPage.getContent().stream()
                .map(homeworkMapper::toHomeworkDTO)
                .collect(Collectors.toList());
        return new PageDTO<>(dtos, homeworkPage.getNumber(), homeworkPage.getSize(), homeworkPage.getTotalElements(), homeworkPage.getTotalPages(), homeworkPage.isLast());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = HOMEWORK_BY_CLASS_STUDENT_CACHE, key = "#classId + '_' + T(com.maxpri.repetgel.utils.SecurityUtils).currentUserKeycloakId.orElse('anonymous') + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public PageDTO<HomeworkStudentViewDTO> getAllHomeworkByClassForStudent(UUID classId, Pageable pageable) {
        String studentId = securityUtils.getCurrentUserKeycloakIdRequired();
        if (!lessonRepository.findAllByClassEntityId(classId, Pageable.ofSize(1)).getContent()
                .stream().findFirst().map(Lesson::getClassEntity).map(ClassEntity::getStudents)
                .map(students -> students.stream().anyMatch(s -> s.getKeycloakUserId().equals(studentId)))
                .orElse(false)) {
            studentRepository.findById(studentId).ifPresent(s -> {
                if (s.getEnrolledClasses().stream().noneMatch(c -> c.getId().equals(classId))) {
                    throw new UnauthorizedOperationException("Student " + studentId + " not enrolled in class " + classId);
                }
            });
        }

        Page<Homework> homeworkPage = homeworkRepository.findAllByLessonClassEntityId(classId, pageable);
        List<HomeworkStudentViewDTO> dtos = homeworkPage.getContent().stream()
                .map(hw -> {
                    HomeworkStudentViewDTO dto = homeworkMapper.toHomeworkStudentViewDTO(hw);
                    if (hw.getTest() != null) {
                        dto.setTest(testMapper.toTestStudentViewDTO(hw.getTest()));
                    }
                    submissionRepository.findByHomeworkIdAndStudentKeycloakUserId(hw.getId(), studentId)
                            .ifPresent(sub -> dto.setMySubmission(submissionMapper.toHomeworkSubmissionDTO(sub)));
                    return dto;
                })
                .collect(Collectors.toList());
        return new PageDTO<>(dtos, homeworkPage.getNumber(), homeworkPage.getSize(), homeworkPage.getTotalElements(), homeworkPage.getTotalPages(), homeworkPage.isLast());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = HOMEWORK_FOR_STUDENT_CACHE, key = "T(com.maxpri.repetgel.utils.SecurityUtils).currentUserKeycloakId.orElse('anonymous') + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public PageDTO<HomeworkStudentViewDTO> getAllHomeworkForCurrentStudent(Pageable pageable) {
        String studentId = securityUtils.getCurrentUserKeycloakIdRequired();
        Page<Homework> homeworkPage = homeworkRepository.findAllByStudentEnrolled(studentId, pageable);
         List<HomeworkStudentViewDTO> dtos = homeworkPage.getContent().stream()
                .map(hw -> {
                    HomeworkStudentViewDTO dto = homeworkMapper.toHomeworkStudentViewDTO(hw);
                    if (hw.getTest() != null) {
                        dto.setTest(testMapper.toTestStudentViewDTO(hw.getTest()));
                    }
                    submissionRepository.findByHomeworkIdAndStudentKeycloakUserId(hw.getId(), studentId)
                        .ifPresent(sub -> dto.setMySubmission(submissionMapper.toHomeworkSubmissionDTO(sub)));
                    return dto;
                })
                .collect(Collectors.toList());
        return new PageDTO<>(dtos, homeworkPage.getNumber(), homeworkPage.getSize(), homeworkPage.getTotalElements(), homeworkPage.getTotalPages(), homeworkPage.isLast());
    }


    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = HOMEWORK_DETAILS_CACHE, key = "#homeworkId + '*'"),
        @CacheEvict(value = HOMEWORK_STUDENT_VIEW_CACHE, key = "#homeworkId + '*'"),
        @CacheEvict(value = HOMEWORK_BY_LESSON_CACHE, allEntries = true),
        @CacheEvict(value = HOMEWORK_BY_CLASS_STUDENT_CACHE, allEntries = true),
        @CacheEvict(value = HOMEWORK_FOR_STUDENT_CACHE, allEntries = true)
    })
    public HomeworkDTO updateHomeworkDetails(UUID homeworkId, HomeworkUpdateDTO homeworkUpdateDTO) {
        String tutorId = securityUtils.getCurrentUserKeycloakIdRequired();
        Homework homework = getHomeworkEntityForTutor(homeworkId, tutorId);
        homeworkMapper.updateEntityFromDto(homeworkUpdateDTO, homework);
        Homework updatedHomework = homeworkRepository.save(homework);
        log.info("Tutor {} updated homework details for {} (ID: {})", tutorId, updatedHomework.getTitle(), updatedHomework.getId());
        return homeworkMapper.toHomeworkDTO(updatedHomework);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = HOMEWORK_DETAILS_CACHE, key = "#homeworkId + '*'"),
            @CacheEvict(value = HOMEWORK_STUDENT_VIEW_CACHE, key = "#homeworkId + '*'"),
            @CacheEvict(value = TEST_DETAILS_CACHE, key = "#homeworkId"),
            @CacheEvict(value = HOMEWORK_BY_LESSON_CACHE, allEntries = true),
            @CacheEvict(value = HOMEWORK_BY_CLASS_STUDENT_CACHE, allEntries = true),
            @CacheEvict(value = HOMEWORK_FOR_STUDENT_CACHE, allEntries = true),
            @CacheEvict(value = SUBMISSIONS_BY_HOMEWORK_CACHE, key = "#homeworkId + '*'"),
            @CacheEvict(value = SUBMISSION_DETAILS_CACHE, allEntries = true)
    })
    public void deleteHomework(UUID homeworkId) {
        String tutorId = securityUtils.getCurrentUserKeycloakIdRequired();
        Homework homework = getHomeworkEntityForTutor(homeworkId, tutorId);
        homework.getSubmissions().forEach(sub -> {
            if (sub.getSubmittedFileKey() != null) {
                try {
                    fileStorageService.deleteFile(sub.getSubmittedFileKey());
                } catch (Exception e) {
                    log.warn("Could not delete submission file {} for homework {}", sub.getSubmittedFileKey(), homeworkId, e);
                }
            }
        });

        homeworkRepository.delete(homework);
        log.info("Tutor {} deleted homework with ID {}", tutorId, homeworkId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = TEST_DETAILS_CACHE, key = "#homeworkId")
    public TestDTO getTestForHomework(UUID homeworkId) {
        String tutorId = securityUtils.getCurrentUserKeycloakIdRequired();
        Homework homework = getHomeworkEntityForTutor(homeworkId, tutorId);
        if (homework.getType() != HomeworkType.TEST || homework.getTest() == null) {
            throw new ResourceNotFoundException("Test not found for homework ID: " + homeworkId + " or homework is not of type TEST.");
        }
        return testMapper.toTestDTO(homework.getTest());
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = TEST_DETAILS_CACHE, key = "#homeworkId"),
        @CacheEvict(value = HOMEWORK_DETAILS_CACHE, key = "#homeworkId + '*'"),
        @CacheEvict(value = HOMEWORK_STUDENT_VIEW_CACHE, key = "#homeworkId + '*'")
    })
    public TestDTO updateTestForHomework(UUID homeworkId, TestCreateDTO testUpdateDTO) {
        String tutorId = securityUtils.getCurrentUserKeycloakIdRequired();
        Homework homework = getHomeworkEntityForTutor(homeworkId, tutorId);

        if (homework.getType() != HomeworkType.TEST) {
            throw new BadRequestException("Cannot update test for homework that is not of type TEST. Homework ID: " + homeworkId);
        }
        Test oldTest = homework.getTest();
        if (oldTest != null) {
            testRepository.delete(oldTest);
            homework.setTest(null);
            homeworkRepository.saveAndFlush(homework);
        }

        Test newTest = testMapper.toEntity(testUpdateDTO);
        newTest.setHomework(homework);
        for (Question question : newTest.getQuestions()) {
            question.setTest(newTest);
            for (AnswerOption option : question.getOptions()) {
                option.setQuestion(question);
            }
        }
        homework.setTest(newTest);

        Homework updatedHomework = homeworkRepository.save(homework);
        log.info("Tutor {} updated test for homework {} (ID: {})", tutorId, homework.getTitle(), homeworkId);
        return testMapper.toTestDTO(updatedHomework.getTest());
    }


    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = SUBMISSION_DETAILS_CACHE, allEntries = true),
            @CacheEvict(value = SUBMISSIONS_BY_HOMEWORK_CACHE, key = "#homeworkId + '*'"),
            @CacheEvict(value = HOMEWORK_STUDENT_VIEW_CACHE, key = "#homeworkId + '_' + T(com.maxpri.repetgel.utils.SecurityUtils).currentUserKeycloakId.orElse('anonymous')")
    })
    public HomeworkSubmissionDTO submitFileUploadHomework(UUID homeworkId, MultipartFile file) {
        Student student = userService.getCurrentStudentEntity();
        Homework homework = getHomeworkEntityForStudent(homeworkId, student.getKeycloakUserId());

        if (homework.getType() != HomeworkType.FILE_UPLOAD) {
            throw new BadRequestException("This homework is not of type FILE_UPLOAD.");
        }
        if (student.getLivesRemaining() != null && student.getLivesRemaining() <= 0) {
            throw new UnauthorizedOperationException("Student has no lives remaining and cannot submit homework.");
        }
        submissionRepository.findByHomeworkIdAndStudentKeycloakUserId(homeworkId, student.getKeycloakUserId())
                .ifPresent(s -> {
                    throw new BadRequestException("You have already submitted this homework.");
                });

        String pathPrefix = "homework_submissions/" + homework.getId().toString() + "/" + student.getKeycloakUserId();
        String s3FileKey = fileStorageService.uploadFile(file, pathPrefix);

        HomeworkSubmission submission = new HomeworkSubmission();
        submission.setHomework(homework);
        submission.setStudent(student);
        submission.setSubmittedFileKey(s3FileKey);
        submission.setSubmittedFileName(file.getOriginalFilename());
        submission.setSubmissionTime(OffsetDateTime.now());
        submission.setLate(submission.getSubmissionTime().isAfter(homework.getDeadline()));

        if (submission.isLate()) {
            userService.deductLife(student);
        }

        HomeworkSubmission savedSubmission = submissionRepository.save(submission);
        log.info("Student {} submitted file for homework {} (ID: {}). S3 Key: {}", student.getKeycloakUserId(), homework.getTitle(), homework.getId(), s3FileKey);

        HomeworkSubmissionDTO dto = submissionMapper.toHomeworkSubmissionDTO(savedSubmission);
        dto.setSubmittedFileDownloadUrl(fileStorageService.getPresignedUrl(s3FileKey, appProperties.getFiles().getPresignedUrlDurationMinutes()));
        return dto;
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = SUBMISSION_DETAILS_CACHE, allEntries = true),
        @CacheEvict(value = SUBMISSIONS_BY_HOMEWORK_CACHE, key = "#homeworkId + '*'"),
        @CacheEvict(value = HOMEWORK_STUDENT_VIEW_CACHE, key = "#homeworkId + '_' + T(com.maxpri.repetgel.utils.SecurityUtils).currentUserKeycloakId.orElse('anonymous')")
    })
    public HomeworkSubmissionDTO submitTestHomework(UUID homeworkId, HomeworkSubmissionRequestDTO submissionRequestDTO) {
        Student student = userService.getCurrentStudentEntity();
        Homework homework = getHomeworkEntityForStudent(homeworkId, student.getKeycloakUserId());

        if (homework.getType() != HomeworkType.TEST) {
            throw new BadRequestException("This homework is not of type TEST.");
        }
        if (homework.getTest() == null) {
            throw new BadRequestException("Test definition not found for this homework.");
        }
        if (student.getLivesRemaining() != null && student.getLivesRemaining() <= 0) {
            throw new UnauthorizedOperationException("Student has no lives remaining and cannot submit homework.");
        }

        submissionRepository.findByHomeworkIdAndStudentKeycloakUserId(homeworkId, student.getKeycloakUserId())
                .ifPresent(s -> {
                    throw new BadRequestException("You have already submitted this homework.");
                });
        if (submissionRequestDTO.getTestAnswers() == null || submissionRequestDTO.getTestAnswers().isEmpty()) {
            throw new BadRequestException("Test answers cannot be empty.");
        }
        List<StudentTestAnswer> studentAnswers = submissionRequestDTO.getTestAnswers().stream()
                .map(dto -> {
                    homework.getTest().getQuestions().stream()
                            .filter(q -> q.getId().equals(dto.getQuestionId()))
                            .findFirst()
                            .orElseThrow(() -> new BadRequestException("Invalid question ID: " + dto.getQuestionId()));

                    homework.getTest().getQuestions().stream()
                            .flatMap(q -> q.getOptions().stream())
                            .filter(opt -> opt.getId().equals(dto.getChosenOptionId()) && opt.getQuestion().getId().equals(dto.getQuestionId()))
                            .findFirst()
                            .orElseThrow(() -> new BadRequestException("Invalid option ID: " + dto.getChosenOptionId() + " for question " + dto.getQuestionId()));

                    return new StudentTestAnswer(dto.getQuestionId(), dto.getChosenOptionId());
                }).collect(Collectors.toList());


        HomeworkSubmission submission = new HomeworkSubmission();
        submission.setHomework(homework);
        submission.setStudent(student);
        submission.setTestAnswers(studentAnswers);
        submission.setSubmissionTime(OffsetDateTime.now());
        submission.setLate(submission.getSubmissionTime().isAfter(homework.getDeadline()));

        if (submission.isLate()) {
            userService.deductLife(student);
        }

        HomeworkSubmission savedSubmission = submissionRepository.save(submission);
        log.info("Student {} submitted test answers for homework {} (ID: {})", student.getKeycloakUserId(), homework.getTitle(), homework.getId());
        return submissionMapper.toHomeworkSubmissionDTO(savedSubmission);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = SUBMISSION_DETAILS_CACHE, key = "#submissionId"),
            @CacheEvict(value = SUBMISSIONS_BY_HOMEWORK_CACHE, allEntries = true),
            @CacheEvict(value = HOMEWORK_STUDENT_VIEW_CACHE, allEntries = true)
    })
    public HomeworkSubmissionDTO gradeSubmission(UUID submissionId, HomeworkSubmissionGradeDTO gradeDTO) {
        String tutorId = securityUtils.getCurrentUserKeycloakIdRequired();
        HomeworkSubmission submission = submissionRepository.findByIdForTutor(submissionId, tutorId)
            .orElseThrow(() -> new ResourceNotFoundException("Submission " + submissionId + " not found or not authorized for tutor " + tutorId));

        submissionMapper.updateEntityFromGradeDto(gradeDTO, submission);
        HomeworkSubmission gradedSubmission = submissionRepository.save(submission);
        log.info("Tutor {} graded submission {} for student {}. Grade: {}", tutorId, submissionId, gradedSubmission.getStudent().getKeycloakUserId(), gradeDTO.getGrade());
        
        HomeworkSubmissionDTO dto = submissionMapper.toHomeworkSubmissionDTO(gradedSubmission);
        if (gradedSubmission.getSubmittedFileKey() != null) {
            dto.setSubmittedFileDownloadUrl(fileStorageService.getPresignedUrl(gradedSubmission.getSubmittedFileKey(), appProperties.getFiles().getPresignedUrlDurationMinutes()));
        }
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = SUBMISSIONS_BY_HOMEWORK_CACHE, key = "#homeworkId + '_tutor_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public PageDTO<HomeworkSubmissionDTO> getSubmissionsForHomeworkByTutor(UUID homeworkId, Pageable pageable) {
        String tutorId = securityUtils.getCurrentUserKeycloakIdRequired();
        getHomeworkEntityForTutor(homeworkId, tutorId);

        Page<HomeworkSubmission> submissionPage = submissionRepository.findAllByHomeworkId(homeworkId, pageable);
        List<HomeworkSubmissionDTO> dtos = submissionPage.getContent().stream()
                .map(sub -> {
                    HomeworkSubmissionDTO dto = submissionMapper.toHomeworkSubmissionDTO(sub);
                    if (sub.getSubmittedFileKey() != null) {
                        dto.setSubmittedFileDownloadUrl(fileStorageService.getPresignedUrl(sub.getSubmittedFileKey(), appProperties.getFiles().getPresignedUrlDurationMinutes()));
                    }
                    return dto;
                })
                .collect(Collectors.toList());
        return new PageDTO<>(dtos, submissionPage.getNumber(), submissionPage.getSize(), submissionPage.getTotalElements(), submissionPage.getTotalPages(), submissionPage.isLast());
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = SUBMISSION_DETAILS_CACHE, key = "#submissionId + '_tutor'")
    public HomeworkSubmissionDTO getSubmissionByIdForTutor(UUID submissionId) {
        String tutorId = securityUtils.getCurrentUserKeycloakIdRequired();
        HomeworkSubmission submission = submissionRepository.findByIdForTutor(submissionId, tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission " + submissionId + " not found for tutor " + tutorId));
        HomeworkSubmissionDTO dto = submissionMapper.toHomeworkSubmissionDTO(submission);
        if (submission.getSubmittedFileKey() != null) {
            dto.setSubmittedFileDownloadUrl(fileStorageService.getPresignedUrl(submission.getSubmittedFileKey(), appProperties.getFiles().getPresignedUrlDurationMinutes()));
        }
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = SUBMISSION_DETAILS_CACHE, key = "#homeworkId + '_student_' + T(com.maxpri.repetgel.utils.SecurityUtils).currentUserKeycloakId.orElse('anonymous')")
    public HomeworkSubmissionDTO getMySubmissionForHomework(UUID homeworkId) {
        String studentId = securityUtils.getCurrentUserKeycloakIdRequired();
        getHomeworkEntityForStudent(homeworkId, studentId);

        HomeworkSubmission submission = submissionRepository.findByHomeworkIdAndStudentKeycloakUserId(homeworkId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("No submission found for homework " + homeworkId + " by student " + studentId));
        HomeworkSubmissionDTO dto = submissionMapper.toHomeworkSubmissionDTO(submission);
        if (submission.getSubmittedFileKey() != null) {
            dto.setSubmittedFileDownloadUrl(fileStorageService.getPresignedUrl(submission.getSubmittedFileKey(), appProperties.getFiles().getPresignedUrlDurationMinutes()));
        }
        return dto;
    }
    
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = HOMEWORK_DETAILS_CACHE, key = "#homeworkId + '*'"),
            @CacheEvict(value = HOMEWORK_STUDENT_VIEW_CACHE, key = "#homeworkId + '_' + #studentId")
    })
    public void extendHomeworkDeadlineForStudent(UUID homeworkId, String studentId, HomeworkDeadlineExtensionDTO extensionDTO) {

        log.warn("Extending deadline for student {} for homework {} to {}. NOTE: This implementation currently modifies the main homework deadline which affects ALL students. A per-student override mechanism is recommended for a production system.", studentId, homeworkId, extensionDTO.getNewDeadline());

        String tutorId = securityUtils.getCurrentUserKeycloakIdRequired();
        Homework homework = getHomeworkEntityForTutor(homeworkId, tutorId);
        studentRepository.findByKeycloakUserId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "ID", studentId));

        if (extensionDTO.getNewDeadline().isBefore(homework.getDeadline())) {
            throw new BadRequestException("New deadline cannot be before the original deadline.");
        }

        homework.setDeadline(extensionDTO.getNewDeadline());
        homeworkRepository.save(homework);
        log.info("Tutor {} extended deadline for homework {} to {} (affects all students)", tutorId, homeworkId, extensionDTO.getNewDeadline());
    }

    @Override
    @Transactional
    @Scheduled(cron = "${app.scheduling.checkOverdueHomeworkCron:0 0 1 * * ?}")
    public void checkAndProcessOverdueHomework() {
        log.info("Scheduler: Starting check for overdue homework without submissions.");
        OffsetDateTime currentTime = OffsetDateTime.now();
        List<Student> allStudents = studentRepository.findAll();

        for (Student student : allStudents) {
            if (student.getLivesRemaining() == null || student.getLivesRemaining() > 0) {
                List<Homework> overdueWithoutSubmission = homeworkRepository.findOverdueHomeworkWithoutSubmissionForStudent(student.getKeycloakUserId(), currentTime);
                for (Homework hw : overdueWithoutSubmission) {
                    log.info("Scheduler: Homework {} (ID: {}) is overdue for student {} and has no submission. Deducting life.", hw.getTitle(), hw.getId(), student.getKeycloakUserId());
                    userService.deductLife(student);
                    if (student.getLivesRemaining() != null && student.getLivesRemaining() <= 0) {
                        log.warn("Scheduler: Student {} has run out of lives after overdue homework processing.", student.getKeycloakUserId());
                        break;
                    }
                }
            }
        }
        log.info("Scheduler: Finished check for overdue homework.");
    }
}