package org.liftakids.service.instituteManage;


import org.liftakids.dto.InstitutionManage.admission.BulkStudentAdmissionRequest;
import org.liftakids.dto.InstitutionManage.admission.StudentAdmissionRequest;
import org.liftakids.dto.InstitutionManage.admission.StudentSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudentAdmissionService {

    // Single admission
    StudentSearchResponse admitStudent(StudentAdmissionRequest request, Long institutionId);

    // Bulk admission
    List<StudentSearchResponse> admitStudents(BulkStudentAdmissionRequest request, Long institutionId);

    // Search students (with option to filter unassigned)
    Page<StudentSearchResponse> searchStudentsForAdmit(String keyword, Long institutionId, Boolean unassignedOnly, Pageable pageable);

    // Get students by class
    List<StudentSearchResponse> getStudentsByClass(Long classId, Long institutionId);

    // Remove student from class (optional)
    void removeStudentFromClass(Long studentId, Long institutionId);
}
