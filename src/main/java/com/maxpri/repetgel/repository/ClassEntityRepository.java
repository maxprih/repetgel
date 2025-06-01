package com.maxpri.repetgel.repository;

import com.maxpri.repetgel.entity.ClassEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassEntityRepository extends JpaRepository<ClassEntity, UUID> {
    Optional<ClassEntity> findByIdAndTutorKeycloakUserId(UUID id, String tutorKeycloakUserId);
    Page<ClassEntity> findAllByTutorKeycloakUserId(String tutorKeycloakUserId, Pageable pageable);

    @Query("SELECT ce FROM ClassEntity ce JOIN ce.students s WHERE s.keycloakUserId = :studentKeycloakUserId")
    Page<ClassEntity> findAllByStudentKeycloakUserId(String studentKeycloakUserId, Pageable pageable);

    boolean existsByIdAndTutorKeycloakUserId(UUID classId, String tutorId);

    @Query("SELECT CASE WHEN COUNT(cs) > 0 THEN TRUE ELSE FALSE END " +
           "FROM ClassEntity c JOIN c.students cs " +
           "WHERE c.id = :classId AND cs.keycloakUserId = :studentId")
    boolean isStudentEnrolled(UUID classId, String studentId);
}