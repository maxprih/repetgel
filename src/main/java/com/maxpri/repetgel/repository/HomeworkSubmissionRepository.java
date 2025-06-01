package com.maxpri.repetgel.repository;

import com.maxpri.repetgel.entity.HomeworkSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HomeworkSubmissionRepository extends JpaRepository<HomeworkSubmission, UUID> {
    Optional<HomeworkSubmission> findByHomeworkIdAndStudentKeycloakUserId(UUID homeworkId, String studentKeycloakUserId);
    Page<HomeworkSubmission> findAllByHomeworkId(UUID homeworkId, Pageable pageable);
    Page<HomeworkSubmission> findAllByStudentKeycloakUserId(String studentKeycloakUserId, Pageable pageable);

    @Query("SELECT hs FROM HomeworkSubmission hs WHERE hs.homework.lesson.classEntity.tutor.keycloakUserId = :tutorId")
    Page<HomeworkSubmission> findAllByTutor(String tutorId, Pageable pageable);

    @Query("SELECT hs FROM HomeworkSubmission hs WHERE hs.homework.lesson.classEntity.tutor.keycloakUserId = :tutorId AND hs.homework.id = :homeworkId")
    Page<HomeworkSubmission> findAllByTutorAndHomeworkId(String tutorId, UUID homeworkId, Pageable pageable);

    @Query("SELECT hs FROM HomeworkSubmission hs WHERE hs.homework.lesson.classEntity.tutor.keycloakUserId = :tutorId AND hs.id = :submissionId")
    Optional<HomeworkSubmission> findByIdForTutor(UUID submissionId, String tutorId);
}