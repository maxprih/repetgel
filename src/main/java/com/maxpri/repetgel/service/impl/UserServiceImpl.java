package com.maxpri.repetgel.service.impl;

import com.maxpri.repetgel.dto.StudentDTO;
import com.maxpri.repetgel.dto.UserDTO;
import com.maxpri.repetgel.dto.UserProfileUpdateDTO;
import com.maxpri.repetgel.entity.Student;
import com.maxpri.repetgel.entity.Tutor;
import com.maxpri.repetgel.entity.User;
import com.maxpri.repetgel.entity.UserRole;
import com.maxpri.repetgel.exception.BadRequestException;
import com.maxpri.repetgel.exception.ResourceNotFoundException;
import com.maxpri.repetgel.mapper.UserMapper;
import com.maxpri.repetgel.repository.StudentRepository;
import com.maxpri.repetgel.repository.TutorRepository;
import com.maxpri.repetgel.repository.UserRepository;
import com.maxpri.repetgel.service.UserService;
import com.maxpri.repetgel.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TutorRepository tutorRepository;
    private final StudentRepository studentRepository;
    private final UserMapper userMapper;

    private static final String USER_PROFILE_CACHE = "userProfile";

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = USER_PROFILE_CACHE, key = "#root.methodName + '_' + T(com.maxpri.repetgel.utils.SecurityUtils).currentUserKeycloakId.orElse('anonymous')")
    public UserDTO getCurrentUserDTO() {
        User user = getCurrentUserEntity();
        return userMapper.toUserDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUserEntity() {
        String keycloakUserId = SecurityUtils.getCurrentUserKeycloakIdRequired();
        return userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "keycloakUserId", keycloakUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public Tutor getCurrentTutorEntity() {
        String keycloakUserId = SecurityUtils.getCurrentUserKeycloakIdRequired();
        return tutorRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor profile not found for keycloakUserId: " + keycloakUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public Student getCurrentStudentEntity() {
        String keycloakUserId = SecurityUtils.getCurrentUserKeycloakIdRequired();
        return studentRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for keycloakUserId: " + keycloakUserId));
    }

    @Override
    @Transactional
    @CacheEvict(value = USER_PROFILE_CACHE, key = "#root.methodName + '_' + T(com.maxpri.repetgel.utils.SecurityUtils).currentUserKeycloakId.orElse('anonymous')")
    public UserDTO updateCurrentUserProfile(UserProfileUpdateDTO profileUpdateDTO) {
        User user = getCurrentUserEntity();
        if (user instanceof Student student) {
            userMapper.updateStudentFromDto(profileUpdateDTO, student);
            return userMapper.toStudentDTO(studentRepository.save(student));
        } else if (user instanceof Tutor tutor) {
            userMapper.updateTutorFromDto(profileUpdateDTO, tutor);
            return userMapper.toTutorDTO(tutorRepository.save(tutor));
        }
        throw new IllegalStateException("User type could not be determined for profile update.");
    }

    @Override
    @Transactional
    public User getOrCreateUser(String keycloakUserId, String email, String firstName, String lastName, UserRole role) {
        return userRepository.findByKeycloakUserId(keycloakUserId).orElseGet(() -> {
            log.info("Creating new user profile for Keycloak ID: {}, Email: {}, Role: {}", keycloakUserId, email, role);
            if (userRepository.existsByEmail(email)) {
                log.warn("Attempting to create user with existing email but different Keycloak ID. Keycloak ID: {}, Email: {}", keycloakUserId, email);
            }

            User newUser;
            if (role == UserRole.STUDENT) {
                Student student = new Student();
                student.setKeycloakUserId(keycloakUserId);
                student.setEmail(email);
                student.setFirstName(firstName);
                student.setLastName(lastName);
                newUser = studentRepository.save(student);
            } else if (role == UserRole.TUTOR) {
                Tutor tutor = new Tutor();
                tutor.setKeycloakUserId(keycloakUserId);
                tutor.setEmail(email);
                tutor.setFirstName(firstName);
                tutor.setLastName(lastName);
                newUser = tutorRepository.save(tutor);
            } else {
                throw new IllegalArgumentException("Unsupported user role: " + role);
            }
            return newUser;
        });
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = USER_PROFILE_CACHE, key = "#studentId")
    public Student getStudentById(String studentId) {
        return studentRepository.findByKeycloakUserId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "ID", studentId));
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = USER_PROFILE_CACHE, key = "#tutorId")
    public Tutor getTutorById(String tutorId) {
         return tutorRepository.findByKeycloakUserId(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor", "ID", tutorId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentDTO> searchStudents(String searchTerm) {
        List<User> users = userRepository.searchUsersByRoleAndTerm(UserRole.STUDENT, searchTerm == null ? "" : searchTerm);
        return users.stream()
                .filter(Student.class::isInstance)
                .map(Student.class::cast)
                .map(userMapper::toStudentDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = USER_PROFILE_CACHE, key = "#student.keycloakUserId")
    public void deductLife(Student student) {
        if (student.getLivesRemaining() == null || student.getLivesRemaining() <= 0) {
            log.warn("Student {} already has 0 or null lives remaining, cannot deduct further.", student.getKeycloakUserId());
            return;
        }
        student.setLivesRemaining(student.getLivesRemaining() - 1);
        studentRepository.save(student);
        log.info("Deducted one life from student {}. Lives remaining: {}", student.getKeycloakUserId(), student.getLivesRemaining());
        if (student.getLivesRemaining() == 0) {
            log.warn("Student {} has run out of lives.", student.getKeycloakUserId());
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = USER_PROFILE_CACHE, key = "#studentId")
    public StudentDTO grantAdditionalLives(String studentId, int livesToAdd) {
        if (livesToAdd <= 0) {
            throw new BadRequestException("Number of lives to add must be positive.");
        }
        Student student = getStudentById(studentId);
        Integer currentLives = student.getLivesRemaining();
        student.setLivesRemaining((currentLives == null ? 0 : currentLives) + livesToAdd);
        Student updatedStudent = studentRepository.save(student);
        log.info("Granted {} additional lives to student {}. New total: {}", livesToAdd, studentId, updatedStudent.getLivesRemaining());
        return userMapper.toStudentDTO(updatedStudent);
    }
}