package com.maxpri.repetgel.repository;

import com.maxpri.repetgel.entity.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TutorRepository extends JpaRepository<Tutor, String> {
    Optional<Tutor> findByKeycloakUserId(String keycloakUserId);

    Optional<Tutor> findByEmail(String email);
}