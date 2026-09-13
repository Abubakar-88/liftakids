package org.liftakids.service.instituteManage;


import org.liftakids.dto.InstitutionManage.ClassSubjectAssignmentDto;
import org.liftakids.dto.InstitutionManage.InstituteSubjectDto;

import java.util.List;

public interface ClassSubjectAssignmentService {

    // Assign subject(s) to class
    List<ClassSubjectAssignmentDto> assignSubjectsToClass(Long classId, List<Long> subjectIds);

    // Remove subject from class
    void removeSubjectFromClass(Long classId, Long subjectId);

    // Remove all subjects from class (optional)
    void removeAllSubjectsFromClass(Long classId);

    // Get all subjects assigned to a class
    List<InstituteSubjectDto> getSubjectsByClass(Long classId);

    // Get assignment details
    List<ClassSubjectAssignmentDto> getAssignmentsByClass(Long classId);

    // Check if subject is assigned to class
    boolean isSubjectAssignedToClass(Long classId, Long subjectId);
}
