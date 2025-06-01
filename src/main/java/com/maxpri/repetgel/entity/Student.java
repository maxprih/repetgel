package com.maxpri.repetgel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@DiscriminatorValue("STUDENT")
public class Student extends User {

    @Column(name = "lives_remaining")
    private Integer livesRemaining = 3;

    @ManyToMany(mappedBy = "students")
    private Set<ClassEntity> enrolledClasses = new HashSet<>();

    @OneToMany(mappedBy = "student")
    private Set<HomeworkSubmission> homeworkSubmissions = new HashSet<>();

}