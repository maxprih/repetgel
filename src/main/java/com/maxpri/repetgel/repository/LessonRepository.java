package com.maxpri.repetgel.repository;

import com.maxpri.repetgel.entity.Lesson;
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
public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    Page<Lesson> findAllByClassEntityId(UUID classId, Pageable pageable);
    Optional<Lesson> findByIdAndClassEntityId(UUID lessonId, UUID classId);

    @Query("SELECT l FROM Lesson l WHERE l.classEntity.tutor.keycloakUserId = :tutorId AND l.startTime >= :rangeStart AND l.endTime <= :rangeEnd")
    List<Lesson> findAllByTutorAndDateRange(String tutorId, OffsetDateTime rangeStart, OffsetDateTime rangeEnd);

    @Query("SELECT l FROM Lesson l JOIN l.classEntity.students s WHERE s.keycloakUserId = :studentId AND l.startTime >= :rangeStart AND l.endTime <= :rangeEnd")
    List<Lesson> findAllByStudentAndDateRange(String studentId, OffsetDateTime rangeStart, OffsetDateTime rangeEnd);
    
    Optional<Lesson> findByIdAndClassEntityTutorKeycloakUserId(UUID lessonId, String tutorKeycloakUserId);
    Optional<Lesson> findByIdAndClassEntityStudentsKeycloakUserId(UUID lessonId, String studentKeycloakUserId);

}