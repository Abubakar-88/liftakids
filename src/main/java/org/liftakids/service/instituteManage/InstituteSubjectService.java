package org.liftakids.service.instituteManage;


import org.liftakids.dto.InstitutionManage.InstituteSubjectDto;

import java.util.List;

public interface InstituteSubjectService {

    // Subject Management
    InstituteSubjectDto createSubject(InstituteSubjectDto requestDto, Long institutionId);
    InstituteSubjectDto updateSubject(Long subjectId, InstituteSubjectDto requestDto, Long institutionId);
    void deleteSubject(Long subjectId, Long institutionId);
    List<InstituteSubjectDto> getAllSubjects(Long institutionId);
    List<InstituteSubjectDto> getActiveSubjects(Long institutionId);
    InstituteSubjectDto getSubjectById(Long subjectId);

    // Bulk Operations
    List<InstituteSubjectDto> createMultipleSubjects(List<InstituteSubjectDto> subjectDtos, Long institutionId);

    // Default Subjects for Institution
    void setupDefaultSubjectsForInstitution(Long institutionId);
}
