package com.maxpri.repetgel.service.impl;

import com.maxpri.repetgel.dto.ClassCreateDTO;
import com.maxpri.repetgel.dto.ClassDetailsDTO;
import com.maxpri.repetgel.dto.ClassInfoDTO;
import com.maxpri.repetgel.dto.ClassUpdateDTO;
import com.maxpri.repetgel.dto.PageDTO;
import com.maxpri.repetgel.dto.StudentDTO;
import com.maxpri.repetgel.entity.ClassEntity;
import com.maxpri.repetgel.entity.Student;
import com.maxpri.repetgel.entity.Tutor;
import com.maxpri.repetgel.exception.BadRequestException;
import com.maxpri.repetgel.exception.ResourceNotFoundException;
import com.maxpri.repetgel.exception.UnauthorizedOperationException;
import com.maxpri.repetgel.mapper.ClassMapper;
import com.maxpri.repetgel.mapper.UserMapper;
import com.maxpri.repetgel.repository.ClassEntityRepository;
import com.maxpri.repetgel.repository.StudentRepository;
import com.maxpri.repetgel.service.ClassService;
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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {

    private final ClassEntityRepository classRepository;
    private final StudentRepository studentRepository;
    private final UserService userService;
    private final ClassMapper classMapper;
    private final UserMapper userMapper;
    private final SecurityUtils securityUtils;

    private static final String CLASS_CACHE_KEY_PREFIX = "'class_details_'";
    private static final String TUTOR_CLASSES_CACHE = "tutorClasses";
    private static final String STUDENT_CLASSES_CACHE = "studentClasses";


    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = TUTOR_CLASSES_CACHE, key = "T(com.maxpri.repetgel.utils.SecurityUtils).currentUserKeycloakId.orElse('anonymous')", allEntries = false)
    })
    public ClassDetailsDTO createClass(ClassCreateDTO classCreateDTO) {
        Tutor currentTutor = userService.getCurrentTutorEntity();
        ClassEntity classEntity = classMapper.toEntity(classCreateDTO);
        classEntity.setTutor(currentTutor);
        ClassEntity savedClass = classRepository.save(classEntity);
        log.info("Tutor {} created class {} with ID {}", currentTutor.getKeycloakUserId(), savedClass.getName(), savedClass.getId());
        return classMapper.toClassDetailsDTO(savedClass);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "classDetails", key = CLASS_CACHE_KEY_PREFIX + " + #classId + '_tutor_' + T(com.maxpri.repetgel.utils.SecurityUtils).currentUserKeycloakId.orElse('anonymous')")
    public ClassDetailsDTO getClassByIdForTutor(UUID classId) {
        String tutorId = securityUtils.getCurrentUserKeycloakIdRequired();
        ClassEntity classEntity = classRepository.findByIdAndTutorKeycloakUserId(classId, tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id " + classId + " for tutor", tutorId));
        return classMapper.toClassDetailsDTO(classEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "classDetails", key = CLASS_CACHE_KEY_PREFIX + " + #classId + '_student_' + T(com.maxpri.repetgel.utils.SecurityUtils).currentUserKeycloakId.orElse('anonymous')")
    public ClassDetailsDTO getClassByIdForStudent(UUID classId) {
        String studentId = securityUtils.getCurrentUserKeycloakIdRequired();
        ClassEntity classEntity = classRepository.findById(classId)
            .orElseThrow(() -> new ResourceNotFoundException("Class", "ID", classId));
        
        boolean isEnrolled = classRepository.isStudentEnrolled(classId, studentId);
        if (!isEnrolled) {
            if (!classEntity.getTutor().getKeycloakUserId().equals(studentId)) {
                throw new UnauthorizedOperationException("Student " + studentId + " is not enrolled in class " + classId);
            }
        }
        return classMapper.toClassDetailsDTO(classEntity);
    }


    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = TUTOR_CLASSES_CACHE, key = "T(com.maxpri.repetgel.utils.SecurityUtils).currentUserKeycloakId.orElse('anonymous') + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public PageDTO<ClassInfoDTO> getAllClassesForCurrentTutor(Pageable pageable) {
        String tutorId = securityUtils.getCurrentUserKeycloakIdRequired();
        Page<ClassEntity> classPage = classRepository.findAllByTutorKeycloakUserId(tutorId, pageable);
        List<ClassInfoDTO> classInfoDTOs = classPage.getContent().stream()
                .map(classMapper::toClassInfoDTO)
                .collect(Collectors.toList());
        return new PageDTO<>(classInfoDTOs, classPage.getNumber(), classPage.getSize(), classPage.getTotalElements(), classPage.getTotalPages(), classPage.isLast());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = STUDENT_CLASSES_CACHE, key = "T(com.maxpri.repetgel.utils.SecurityUtils).currentUserKeycloakId.orElse('anonymous') + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public PageDTO<ClassInfoDTO> getAllClassesForCurrentStudent(Pageable pageable) {
        String studentId = securityUtils.getCurrentUserKeycloakIdRequired();
        Page<ClassEntity> classPage = classRepository.findAllByStudentKeycloakUserId(studentId, pageable);
         List<ClassInfoDTO> classInfoDTOs = classPage.getContent().stream()
                .map(classMapper::toClassInfoDTO)
                .collect(Collectors.toList());
        return new PageDTO<>(classInfoDTOs, classPage.getNumber(), classPage.getSize(), classPage.getTotalElements(), classPage.getTotalPages(), classPage.isLast());
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "classDetails", key = CLASS_CACHE_KEY_PREFIX + " + #classId + '*'"),
            @CacheEvict(value = TUTOR_CLASSES_CACHE, allEntries = true),
            @CacheEvict(value = STUDENT_CLASSES_CACHE, allEntries = true)
    })
    public ClassDetailsDTO updateClass(UUID classId, ClassUpdateDTO classUpdateDTO) {
        Tutor currentTutor = userService.getCurrentTutorEntity();
        ClassEntity classEntity = classRepository.findByIdAndTutorKeycloakUserId(classId, currentTutor.getKeycloakUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found or you are not authorized to update it. Class ID: " + classId));
        
        classMapper.updateEntityFromDto(classUpdateDTO, classEntity);
        ClassEntity updatedClass = classRepository.save(classEntity);
        log.info("Tutor {} updated class {} (ID: {})", currentTutor.getKeycloakUserId(), updatedClass.getName(), updatedClass.getId());
        return classMapper.toClassDetailsDTO(updatedClass);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "classDetails", key = CLASS_CACHE_KEY_PREFIX + " + #classId + '*'"),
        @CacheEvict(value = TUTOR_CLASSES_CACHE, allEntries = true),
        @CacheEvict(value = STUDENT_CLASSES_CACHE, allEntries = true)
    })
    public void deleteClass(UUID classId) {
        Tutor currentTutor = userService.getCurrentTutorEntity();
        ClassEntity classEntity = classRepository.findByIdAndTutorKeycloakUserId(classId, currentTutor.getKeycloakUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found or you are not authorized to delete it. Class ID: " + classId));
        classRepository.delete(classEntity);
        log.info("Tutor {} deleted class with ID {}", currentTutor.getKeycloakUserId(), classId);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "classDetails", key = CLASS_CACHE_KEY_PREFIX + " + #classId + '*'"),
            @CacheEvict(value = TUTOR_CLASSES_CACHE, allEntries = true),
            @CacheEvict(value = STUDENT_CLASSES_CACHE, allEntries = true)
    })
    public ClassDetailsDTO addStudentToClass(UUID classId, String studentEmail) {
        Tutor currentTutor = userService.getCurrentTutorEntity();
        ClassEntity classEntity = classRepository.findByIdAndTutorKeycloakUserId(classId, currentTutor.getKeycloakUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found or you are not authorized. Class ID: " + classId));

        Student student = studentRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "email", studentEmail));

        if (classEntity.getStudents().contains(student)) {
            throw new BadRequestException("Student " + studentEmail + " is already enrolled in class " + classEntity.getName());
        }

        classEntity.getStudents().add(student);
        student.getEnrolledClasses().add(classEntity);
        classRepository.save(classEntity);

        log.info("Tutor {} added student {} (Email: {}) to class {} (ID: {})", currentTutor.getKeycloakUserId(), student.getKeycloakUserId(), studentEmail, classEntity.getName(), classId);
        return classMapper.toClassDetailsDTO(classEntity);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "classDetails", key = CLASS_CACHE_KEY_PREFIX + " + #classId + '*'"),
            @CacheEvict(value = TUTOR_CLASSES_CACHE, allEntries = true),
            @CacheEvict(value = STUDENT_CLASSES_CACHE, allEntries = true)
    })
    public ClassDetailsDTO removeStudentFromClass(UUID classId, String studentKeycloakId) {
        Tutor currentTutor = userService.getCurrentTutorEntity();
        ClassEntity classEntity = classRepository.findByIdAndTutorKeycloakUserId(classId, currentTutor.getKeycloakUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found or you are not authorized. Class ID: " + classId));

        Student student = studentRepository.findByKeycloakUserId(studentKeycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "ID", studentKeycloakId));

        if (!classEntity.getStudents().contains(student)) {
            throw new BadRequestException("Student " + studentKeycloakId + " is not enrolled in class " + classEntity.getName());
        }

        classEntity.getStudents().remove(student);
        student.getEnrolledClasses().remove(classEntity);
        classRepository.save(classEntity);

        log.info("Tutor {} removed student {} from class {} (ID: {})", currentTutor.getKeycloakUserId(), studentKeycloakId, classEntity.getName(), classId);
        return classMapper.toClassDetailsDTO(classEntity);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "classEnrolledStudents", key = "#classId")
    public List<StudentDTO> getEnrolledStudents(UUID classId) {
        Tutor currentTutor = userService.getCurrentTutorEntity();
        if (!classRepository.existsByIdAndTutorKeycloakUserId(classId, currentTutor.getKeycloakUserId())) {
            throw new ResourceNotFoundException("Class not found or you are not authorized to view its students. Class ID: " + classId);
        }

        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class", "ID", classId));

        return classEntity.getStudents().stream()
                .map(userMapper::toStudentDTO)
                .collect(Collectors.toList());
    }
}