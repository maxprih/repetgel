package com.maxpri.repetgel.mapper;

import com.maxpri.repetgel.dto.StudentDTO;
import com.maxpri.repetgel.dto.TutorDTO;
import com.maxpri.repetgel.dto.UserDTO;
import com.maxpri.repetgel.dto.UserProfileUpdateDTO;
import com.maxpri.repetgel.entity.Student;
import com.maxpri.repetgel.entity.Tutor;
import com.maxpri.repetgel.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    @Mapping(target = "role", source = "roleEnum")
    TutorDTO toTutorDTO(Tutor tutor);
    @Mapping(target = "role", source = "roleEnum")
    StudentDTO toStudentDTO(Student student);

    default UserDTO toUserDTO(User user) {
        if (user instanceof Tutor) {
            return toTutorDTO((Tutor) user);
        } else if (user instanceof Student) {
            return toStudentDTO((Student) user);
        }
        return null;
    }

    void updateStudentFromDto(UserProfileUpdateDTO dto, @MappingTarget Student student);
    void updateTutorFromDto(UserProfileUpdateDTO dto, @MappingTarget Tutor tutor);
}