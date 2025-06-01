package com.maxpri.repetgel.service;

import com.maxpri.repetgel.dto.StudentDTO;
import com.maxpri.repetgel.dto.UserDTO;
import com.maxpri.repetgel.dto.UserProfileUpdateDTO;
import com.maxpri.repetgel.entity.Student;
import com.maxpri.repetgel.entity.Tutor;
import com.maxpri.repetgel.entity.User;
import com.maxpri.repetgel.entity.UserRole;

import java.util.List;

public interface UserService {
    UserDTO getCurrentUserDTO();
    User getCurrentUserEntity();
    Tutor getCurrentTutorEntity();
    Student getCurrentStudentEntity();
    UserDTO updateCurrentUserProfile(UserProfileUpdateDTO profileUpdateDTO);
    User getOrCreateUser(String keycloakUserId, String email, String firstName, String lastName, UserRole role);
    Student getStudentById(String studentId);
    Tutor getTutorById(String tutorId);
    List<StudentDTO> searchStudents(String searchTerm);

    void deductLife(Student student);
    StudentDTO grantAdditionalLives(String studentId, int livesToAdd);

}