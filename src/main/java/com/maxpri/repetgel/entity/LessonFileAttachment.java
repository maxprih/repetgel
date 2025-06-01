package com.maxpri.repetgel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "lesson_file_attachment", uniqueConstraints = {
        @UniqueConstraint(name = "uq_lessonfile_s3key", columnNames = {"s3_file_key"})
})
public class LessonFileAttachment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "s3_file_key", nullable = false, length = 1024)
    private String s3FileKey;

    @Column(name = "file_type", length = 100)
    private String fileType;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private OffsetDateTime uploadedAt;

    @Override
    @Transient
    public OffsetDateTime getCreatedAt() {
        return uploadedAt;
    }

    @Override
    @Transient
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.uploadedAt = createdAt;
    }

    @Override
    @Transient
    public OffsetDateTime getUpdatedAt() {
        return null;
    }

    @Override
    @Transient
    public void setUpdatedAt(OffsetDateTime updatedAt) {
    }
}