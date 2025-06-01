package com.maxpri.repetgel.service;

import com.maxpri.repetgel.dto.HomeworkCreateDTO;
import com.maxpri.repetgel.dto.HomeworkDTO;
import com.maxpri.repetgel.dto.HomeworkDeadlineExtensionDTO;
import com.maxpri.repetgel.dto.HomeworkStudentViewDTO;
import com.maxpri.repetgel.dto.HomeworkSubmissionDTO;
import com.maxpri.repetgel.dto.HomeworkSubmissionGradeDTO;
import com.maxpri.repetgel.dto.HomeworkSubmissionRequestDTO;
import com.maxpri.repetgel.dto.HomeworkUpdateDTO;
import com.maxpri.repetgel.dto.PageDTO;
import com.maxpri.repetgel.dto.TestCreateDTO;
import com.maxpri.repetgel.dto.TestDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface HomeworkService {
    HomeworkDTO createHomework(HomeworkCreateDTO homeworkCreateDTO);

    HomeworkDTO getHomeworkByIdForTutor(UUID homeworkId);

    HomeworkStudentViewDTO getHomeworkByIdForStudent(UUID homeworkId);

    PageDTO<HomeworkDTO> getAllHomeworkByLessonForTutor(UUID lessonId, Pageable pageable);

    PageDTO<HomeworkStudentViewDTO> getAllHomeworkByClassForStudent(UUID classId, Pageable pageable);

    PageDTO<HomeworkStudentViewDTO> getAllHomeworkForCurrentStudent(Pageable pageable);

    HomeworkDTO updateHomeworkDetails(UUID homeworkId, HomeworkUpdateDTO homeworkUpdateDTO);

    void deleteHomework(UUID homeworkId);

    TestDTO getTestForHomework(UUID homeworkId);

    TestDTO updateTestForHomework(UUID homeworkId, TestCreateDTO testCreateDTO);

    HomeworkSubmissionDTO submitFileUploadHomework(UUID homeworkId, MultipartFile file);

    HomeworkSubmissionDTO submitTestHomework(UUID homeworkId, HomeworkSubmissionRequestDTO submissionRequestDTO);

    HomeworkSubmissionDTO gradeSubmission(UUID submissionId, HomeworkSubmissionGradeDTO gradeDTO);

    PageDTO<HomeworkSubmissionDTO> getSubmissionsForHomeworkByTutor(UUID homeworkId, Pageable pageable);

    HomeworkSubmissionDTO getSubmissionByIdForTutor(UUID submissionId);

    HomeworkSubmissionDTO getMySubmissionForHomework(UUID homeworkId);

    void extendHomeworkDeadlineForStudent(UUID homeworkId, String studentId, HomeworkDeadlineExtensionDTO extensionDTO);

    void checkAndProcessOverdueHomework();
}