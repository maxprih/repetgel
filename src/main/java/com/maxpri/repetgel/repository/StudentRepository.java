package com.maxpri.repetgel.repository;

import com.maxpri.repetgel.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {
    Optional<Student> findByKeycloakUserId(String keycloakUserId);

    Optional<Student> findByEmail(String email);

    @Query("SELECT s FROM Student s JOIN s.enrolledClasses ec WHERE ec.id = :classId")
    List<Student> findAllByClassId(UUID classId);
}