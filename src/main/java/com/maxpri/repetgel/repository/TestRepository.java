package com.maxpri.repetgel.repository;

import com.maxpri.repetgel.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TestRepository extends JpaRepository<Test, UUID> {
    Optional<Test> findByHomeworkId(UUID homeworkId);
}