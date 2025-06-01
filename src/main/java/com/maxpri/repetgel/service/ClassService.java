package com.maxpri.repetgel.service;

import com.maxpri.repetgel.dto.ClassCreateDTO;
import com.maxpri.repetgel.dto.ClassDetailsDTO;
import com.maxpri.repetgel.dto.ClassInfoDTO;
import com.maxpri.repetgel.dto.ClassUpdateDTO;
import com.maxpri.repetgel.dto.PageDTO;
import com.maxpri.repetgel.dto.StudentDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ClassService {
    ClassDetailsDTO createClass(ClassCreateDTO classCreateDTO);
    ClassDetailsDTO getClassByIdForTutor(UUID classId);
    ClassDetailsDTO getClassByIdForStudent(UUID classId);
    PageDTO<ClassInfoDTO> getAllClassesForCurrentTutor(Pageable pageable);
    PageDTO<ClassInfoDTO> getAllClassesForCurrentStudent(Pageable pageable);
    ClassDetailsDTO updateClass(UUID classId, ClassUpdateDTO classUpdateDTO);
    void deleteClass(UUID classId);

    ClassDetailsDTO addStudentToClass(UUID classId, String studentEmail);
    ClassDetailsDTO removeStudentFromClass(UUID classId, String studentId);
    List<StudentDTO> getEnrolledStudents(UUID classId);
}