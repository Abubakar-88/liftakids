package org.liftakids.service.instituteManage.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.liftakids.dto.InstitutionManage.admission.BulkStudentAdmissionRequest;
import org.liftakids.dto.InstitutionManage.admission.StudentAdmissionRequest;
import org.liftakids.dto.InstitutionManage.admission.StudentSearchResponse;
import org.liftakids.entity.InstituteManage.InstituteClass;
import org.liftakids.entity.Student;
import org.liftakids.exception.ResourceNotFoundException;
import org.liftakids.exception.UnauthorizedException;
import org.liftakids.repositories.instituteManage.InstituteClassRepository;
import org.liftakids.repositories.instituteManage.StudentAdmissionRepository;
import org.liftakids.service.instituteManage.StudentAdmissionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentAdmissionServiceImpl implements StudentAdmissionService {

    private final StudentAdmissionRepository studentAdmissionRepository;
    private final InstituteClassRepository classRepository;

    @Override
    @Transactional
    public StudentSearchResponse admitStudent(StudentAdmissionRequest request, Long institutionId) {
        log.info("Admitting student {} to class {}", request.getStudentId(), request.getClassId());

        Student student = studentAdmissionRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        // Check institution ownership (assuming student is linked to institution)
        if (!student.getInstitution().getInstitutionsId().equals(institutionId)) {
            throw new UnauthorizedException("Student does not belong to this institution");
        }

        InstituteClass instituteClass = classRepository.findByIdAndInstitution_InstitutionsId(
                        request.getClassId(), institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found in this institution"));

        // If student already in a class, we can either update or throw
        if (student.getInstituteClass() != null) {
            log.warn("Student {} is already in class {}, overriding", student.getStudentId(), student.getInstituteClass().getClassName());
        }

        student.setInstituteClass(instituteClass);
        studentAdmissionRepository.save(student);

        return convertToSearchResponse(student);
    }

    @Override
    @Transactional
    public List<StudentSearchResponse> admitStudents(BulkStudentAdmissionRequest request, Long institutionId) {
        log.info("Bulk admitting {} students to class {}", request.getStudentIds().size(), request.getClassId());

        InstituteClass instituteClass = classRepository.findByIdAndInstitution_InstitutionsId(
                        request.getClassId(), institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found in this institution"));

        List<StudentSearchResponse> responses = new ArrayList<>();
        for (Long studentId : request.getStudentIds()) {
            Student student = studentAdmissionRepository.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));

            if (!student.getInstitution().getInstitutionsId().equals(institutionId)) {
                throw new UnauthorizedException("Student " + studentId + " does not belong to this institution");
            }

            student.setInstituteClass(instituteClass);
            studentAdmissionRepository.save(student);
            responses.add(convertToSearchResponse(student));
        }

        return responses;
    }

    @Override
    @Transactional
    public Page<StudentSearchResponse> searchStudentsForAdmit(String keyword, Long institutionId, Boolean unassignedOnly, Pageable pageable) {
        log.info("Searching students with keyword: {}, unassignedOnly: {}, institutionId: {}", keyword, unassignedOnly, institutionId);

        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        boolean filterUnassigned = (unassignedOnly != null && unassignedOnly);

        // ✅ নতুন Repository মেথড কল করুন
        Page<Student> studentPage = studentAdmissionRepository.searchStudentsByInstitutionWithFilters(
                institutionId, searchKeyword, filterUnassigned, pageable);

        return studentPage.map(this::convertToSearchResponse);
    }
    @Override
    @Transactional
    public List<StudentSearchResponse> getStudentsByClass(Long classId, Long institutionId) {
        // Verify class belongs to institution
        classRepository.findByIdAndInstitution_InstitutionsId(classId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        List<Student> students = studentAdmissionRepository.findByInstituteClass_Id(classId);
        return students.stream()
                .map(this::convertToSearchResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeStudentFromClass(Long studentId, Long institutionId) {
        Student student = studentAdmissionRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (!student.getInstitution().getInstitutionsId().equals(institutionId)) {
            throw new UnauthorizedException("Student does not belong to this institution");
        }

        student.setInstituteClass(null);
        studentAdmissionRepository.save(student);
        log.info("Student {} removed from class", studentId);
    }

    // ========== Helper ==========
    private StudentSearchResponse convertToSearchResponse(Student student) {
        return StudentSearchResponse.builder()
                .studentId(student.getStudentId())
                .studentName(student.getStudentName())
                .guardianName(student.getGuardianName())
                .contactNumber(student.getContactNumber())
                .className(student.getInstituteClass() != null ? student.getInstituteClass().getClassName() : null)
                .isSponsored(student.isSponsored())
                .isAdmitted(student.getInstituteClass() != null)
                .build();
    }
}
