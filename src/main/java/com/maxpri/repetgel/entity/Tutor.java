package com.maxpri.repetgel.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@DiscriminatorValue("TUTOR")
public class Tutor extends User {

    @OneToMany(mappedBy = "tutor")
    private Set<ClassEntity> managedClasses = new HashSet<>();

}