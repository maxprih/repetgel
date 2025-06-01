package com.maxpri.repetgel.mapper;

import com.maxpri.repetgel.dto.AnswerOptionCreateDTO;
import com.maxpri.repetgel.dto.AnswerOptionDTO;
import com.maxpri.repetgel.dto.AnswerOptionStudentViewDTO;
import com.maxpri.repetgel.dto.QuestionCreateDTO;
import com.maxpri.repetgel.dto.QuestionDTO;
import com.maxpri.repetgel.dto.QuestionStudentViewDTO;
import com.maxpri.repetgel.dto.TestCreateDTO;
import com.maxpri.repetgel.dto.TestDTO;
import com.maxpri.repetgel.dto.TestStudentViewDTO;
import com.maxpri.repetgel.entity.AnswerOption;
import com.maxpri.repetgel.entity.Question;
import com.maxpri.repetgel.entity.Test;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TestMapper {

    AnswerOptionDTO toAnswerOptionDTO(AnswerOption answerOption);
    AnswerOptionStudentViewDTO toAnswerOptionStudentViewDTO(AnswerOption answerOption);
    List<AnswerOptionStudentViewDTO> toAnswerOptionStudentViewDTOs(List<AnswerOption> answerOptions);

    QuestionDTO toQuestionDTO(Question question);
    @Mapping(target = "options", expression = "java(toAnswerOptionStudentViewDTOs(question.getOptions()))")
    QuestionStudentViewDTO toQuestionStudentViewDTO(Question question);

    @Mapping(target = "homeworkId", source = "homework.id")
    TestDTO toTestDTO(Test test);

    @Mapping(target = "homeworkId", source = "homework.id")
    @Mapping(target = "questions", source = "questions")
    TestStudentViewDTO toTestStudentViewDTO(Test test);

    AnswerOption toEntity(AnswerOptionCreateDTO dto);
    Question toEntity(QuestionCreateDTO dto);
    Test toEntity(TestCreateDTO dto);

}