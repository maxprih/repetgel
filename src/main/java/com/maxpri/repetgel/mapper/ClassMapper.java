package com.maxpri.repetgel.mapper;

import com.maxpri.repetgel.dto.ClassCreateDTO;
import com.maxpri.repetgel.dto.ClassDetailsDTO;
import com.maxpri.repetgel.dto.ClassInfoDTO;
import com.maxpri.repetgel.dto.ClassUpdateDTO;
import com.maxpri.repetgel.entity.ClassEntity;
import com.maxpri.repetgel.entity.Tutor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {UserMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ClassMapper {

    @Mapping(target = "tutorId", source = "classEntity.tutor.keycloakUserId")
    @Mapping(target = "tutorName", expression = "java(classEntity.getTutor() != null ? classEntity.getTutor().getFirstName() + \" \" + classEntity.getTutor().getLastName() : null)")
    @Mapping(target = "studentCount", expression = "java((long)classEntity.getStudents().size())")
    ClassInfoDTO toClassInfoDTO(ClassEntity classEntity);

    @Mapping(target = "tutorId", source = "classEntity.tutor.keycloakUserId")
    @Mapping(target = "tutorName", expression = "java(classEntity.getTutor() != null ? classEntity.getTutor().getFirstName() + \" \" + classEntity.getTutor().getLastName() : null)")
    @Mapping(target = "studentCount", expression = "java((long)classEntity.getStudents().size())")
    @Mapping(target = "students", source = "students")
    ClassDetailsDTO toClassDetailsDTO(ClassEntity classEntity);

    ClassEntity toEntity(ClassCreateDTO dto);

    void updateEntityFromDto(ClassUpdateDTO dto, @MappingTarget ClassEntity entity);

    @Named("getTutorFromClass")
    default Tutor getTutorFromClass(ClassEntity classEntity) {
        return classEntity.getTutor();
    }
}