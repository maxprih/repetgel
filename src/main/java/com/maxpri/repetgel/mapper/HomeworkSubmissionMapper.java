package com.maxpri.repetgel.mapper;

import com.maxpri.repetgel.dto.HomeworkSubmissionDTO;
import com.maxpri.repetgel.dto.HomeworkSubmissionGradeDTO;
import com.maxpri.repetgel.dto.StudentTestAnswerDTO;
import com.maxpri.repetgel.entity.HomeworkSubmission;
import com.maxpri.repetgel.entity.StudentTestAnswer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface HomeworkSubmissionMapper {

    StudentTestAnswer toEntity(StudentTestAnswerDTO dto);
    List<StudentTestAnswer> toEntityList(List<StudentTestAnswerDTO> dtoList);

    StudentTestAnswerDTO toDto(StudentTestAnswer entity);
    List<StudentTestAnswerDTO> toDtoList(List<StudentTestAnswer> entityList);


    @Mapping(target = "homeworkId", source = "homework.id")
    @Mapping(target = "studentId", source = "student.keycloakUserId")
    @Mapping(target = "studentName", expression = "java(submission.getStudent() != null ? submission.getStudent().getFirstName() + \" \" + submission.getStudent().getLastName() : null)")
    @Mapping(target = "submittedFileDownloadUrl", ignore = true)
    HomeworkSubmissionDTO toHomeworkSubmissionDTO(HomeworkSubmission submission);

    void updateEntityFromGradeDto(HomeworkSubmissionGradeDTO dto, @MappingTarget HomeworkSubmission entity);
}