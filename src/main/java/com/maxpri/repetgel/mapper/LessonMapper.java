package com.maxpri.repetgel.mapper;

import com.maxpri.repetgel.dto.LessonCreateDTO;
import com.maxpri.repetgel.dto.LessonDTO;
import com.maxpri.repetgel.dto.LessonFileAttachmentDTO;
import com.maxpri.repetgel.dto.LessonUpdateDTO;
import com.maxpri.repetgel.entity.Lesson;
import com.maxpri.repetgel.entity.LessonFileAttachment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LessonMapper {

    @Mapping(target = "downloadUrl", ignore = true)
    LessonFileAttachmentDTO toLessonFileAttachmentDTO(LessonFileAttachment attachment);

    @Mapping(target = "classId", source = "classEntity.id")
    LessonDTO toLessonDTO(Lesson lesson);

    Lesson toEntity(LessonCreateDTO dto);

    void updateEntityFromDto(LessonUpdateDTO dto, @MappingTarget Lesson entity);
}