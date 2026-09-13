package org.liftakids.service.instituteManage;


import org.liftakids.dto.InstitutionManage.InstituteClassDto;

import java.util.List;

public interface InstituteClassService {

    // Class Management
    InstituteClassDto createClass(InstituteClassDto requestDto, Long institutionId);
    InstituteClassDto updateClass(Long classId, InstituteClassDto requestDto, Long institutionId);
    void deleteClass(Long classId, Long institutionId);
    List<InstituteClassDto> getAllClasses(Long institutionId);
    List<InstituteClassDto> getActiveClasses(Long institutionId);
    InstituteClassDto getClassById(Long classId);

    // Student Count
    long getStudentCountByClass(Long classId);

    // Bulk Operations
    List<InstituteClassDto> createMultipleClasses(List<InstituteClassDto> classDtos, Long institutionId);

    // Default Classes for Institution
    void setupDefaultClassesForInstitution(Long institutionId);
}
