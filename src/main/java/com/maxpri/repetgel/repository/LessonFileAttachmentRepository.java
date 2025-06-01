package com.maxpri.repetgel.repository;

import com.maxpri.repetgel.entity.LessonFileAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LessonFileAttachmentRepository extends JpaRepository<LessonFileAttachment, UUID> {
    List<LessonFileAttachment> findAllByLessonId(UUID lessonId);
    Optional<LessonFileAttachment> findByIdAndLessonId(UUID attachmentId, UUID lessonId);
    Optional<LessonFileAttachment> findByS3FileKey(String s3FileKey);
}