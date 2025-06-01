package com.maxpri.repetgel.repository;

import com.maxpri.repetgel.entity.Homework;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HomeworkRepository extends JpaRepository<Homework, UUID> {
    Page<Homework> findAllByLessonClassEntityId(UUID classId, Pageable pageable);
    Page<Homework> findAllByLessonId(UUID lessonId, Pageable pageable);
    Optional<Homework> findByIdAndLessonId(UUID homeworkId, UUID lessonId);

    @Query("SELECT h FROM Homework h JOIN h.lesson.classEntity.students s WHERE s.keycloakUserId = :studentId")
    Page<Homework> findAllByStudentEnrolled(String studentId, Pageable pageable);

    @Query("SELECT h FROM Homework h JOIN h.lesson.classEntity.students s WHERE s.keycloakUserId = :studentId AND h.id = :homeworkId")
    Optional<Homework> findByIdForStudent(UUID homeworkId, String studentId);

    @Query("SELECT h FROM Homework h WHERE h.lesson.classEntity.tutor.keycloakUserId = :tutorId")
    Page<Homework> findAllByTutor(String tutorId, Pageable pageable);
    
    @Query("SELECT h FROM Homework h WHERE h.lesson.classEntity.tutor.keycloakUserId = :tutorId AND h.id = :homeworkId")
    Optional<Homework> findByIdForTutor(UUID homeworkId, String tutorId);

    @Query("SELECT h FROM Homework h " +
            "JOIN h.lesson.classEntity.students s " +
            "WHERE s.keycloakUserId = :studentId " +
            "AND h.deadline < :currentTime " +
            "AND NOT EXISTS (SELECT hs FROM HomeworkSubmission hs WHERE hs.homework = h AND hs.student.keycloakUserId = :studentId)")
    List<Homework> findOverdueHomeworkWithoutSubmissionForStudent(String studentId, OffsetDateTime currentTime);
}