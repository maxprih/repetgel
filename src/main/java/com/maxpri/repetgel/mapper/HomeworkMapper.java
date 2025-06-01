package com.maxpri.repetgel.mapper;

import com.maxpri.repetgel.dto.HomeworkCreateDTO;
import com.maxpri.repetgel.dto.HomeworkDTO;
import com.maxpri.repetgel.dto.HomeworkStudentViewDTO;
import com.maxpri.repetgel.dto.HomeworkUpdateDTO;
import com.maxpri.repetgel.dto.TestStudentViewDTO;
import com.maxpri.repetgel.entity.Homework;
import com.maxpri.repetgel.entity.Lesson;
import com.maxpri.repetgel.entity.Test;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.UUID;

@Mapper(componentModel = "spring", uses = {TestMapper.class, HomeworkSubmissionMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface HomeworkMapper {

    @Mapping(target = "lessonId", source = "lesson.id")
    HomeworkDTO toHomeworkDTO(Homework homework);

    @Mapping(target = "lessonId", source = "homework.lesson.id")
    @Mapping(target = "lessonTitle", source = "homework.lesson.title")
    @Mapping(target = "mySubmission", ignore = true)
    HomeworkStudentViewDTO toHomeworkStudentViewDTO(Homework homework);

    @Mapping(target = "lesson", source = "lessonId", qualifiedByName = "lessonFromId")
    Homework toEntity(HomeworkCreateDTO dto);

    void updateEntityFromDto(HomeworkUpdateDTO dto, @MappingTarget Homework entity);

    @Named("lessonFromId")
    default Lesson lessonFromId(UUID lessonId) {
        if (lessonId == null) {
            return null;
        }
        Lesson lesson = new Lesson();
        lesson.setId(lessonId);
        return lesson;
    }

    @Named("toTestStudentViewDTO")
    default TestStudentViewDTO mapTestToStudentView(Test test, @Context TestMapper testMapper) {
        return testMapper.toTestStudentViewDTO(test);
    }
}