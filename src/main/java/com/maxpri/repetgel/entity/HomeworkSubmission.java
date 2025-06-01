package com.maxpri.repetgel.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
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
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "homework_submission", uniqueConstraints = {
    @UniqueConstraint(name = "uq_homework_submission_student_homework", columnNames = {"homework_id", "student_id"})
})
public class HomeworkSubmission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "homework_id", nullable = false)
    private Homework homework;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "keycloak_user_id", nullable = false)
    private Student student;

    @CreationTimestamp
    @Column(name = "submission_time", nullable = false, updatable = false)
    private OffsetDateTime submissionTime;

    @Column(name = "submitted_file_key", length = 1024)
    private String submittedFileKey;

    @Column(name = "submitted_file_name")
    private String submittedFileName;

    @Type(JsonType.class)
    @Column(name = "test_answers", columnDefinition = "jsonb")
    private List<StudentTestAnswer> testAnswers;

    @Column(name = "grade")
    private Integer grade;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "is_late", nullable = false)
    private boolean isLate = false;


    @Override
    @Transient
    public OffsetDateTime getCreatedAt() {
        return submissionTime;
    }

    @Override
    @Transient
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.submissionTime = createdAt;
    }
}