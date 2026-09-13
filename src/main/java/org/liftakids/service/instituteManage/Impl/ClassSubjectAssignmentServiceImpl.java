package org.liftakids.service.instituteManage.Impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.liftakids.dto.InstitutionManage.ClassSubjectAssignmentDto;
import org.liftakids.dto.InstitutionManage.InstituteSubjectDto;
import org.liftakids.entity.InstituteManage.ClassSubjectAssignment;
import org.liftakids.entity.InstituteManage.InstituteClass;
import org.liftakids.entity.InstituteManage.InstituteSubject;
import org.liftakids.exception.ResourceNotFoundException;
import org.liftakids.repositories.instituteManage.ClassSubjectAssignmentRepository;
import org.liftakids.repositories.instituteManage.InstituteClassRepository;
import org.liftakids.repositories.instituteManage.InstituteSubjectRepository;
import org.liftakids.service.instituteManage.ClassSubjectAssignmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassSubjectAssignmentServiceImpl implements ClassSubjectAssignmentService {

    private final ClassSubjectAssignmentRepository assignmentRepository;
    private final InstituteClassRepository classRepository;
    private final InstituteSubjectRepository subjectRepository;

    @Override
    @Transactional
    public List<ClassSubjectAssignmentDto> assignSubjectsToClass(Long classId, List<Long> subjectIds) {
        log.info("Assigning {} subjects to class {}", subjectIds.size(), classId);

        InstituteClass instituteClass = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        List<ClassSubjectAssignment> assignments = new ArrayList<>();

        for (Long subjectId : subjectIds) {
            // Check if already assigned
            if (assignmentRepository.existsByInstituteClass_IdAndSubject_Id(classId, subjectId)) {
                log.warn("Subject {} already assigned to class {}, skipping", subjectId, classId);
                continue;
            }

            InstituteSubject subject = subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + subjectId));

            ClassSubjectAssignment assignment = ClassSubjectAssignment.builder()
                    .instituteClass(instituteClass)
                    .subject(subject)
                    .isActive(true)
                    .build();

            assignments.add(assignmentRepository.save(assignment));
        }

        log.info("Assigned {} new subjects to class {}", assignments.size(), classId);
        return assignments.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeSubjectFromClass(Long classId, Long subjectId) {
        log.info("Removing subject {} from class {}", subjectId, classId);

        if (!assignmentRepository.existsByInstituteClass_IdAndSubject_Id(classId, subjectId)) {
            throw new ResourceNotFoundException("Subject not assigned to this class");
        }

        assignmentRepository.deleteByClassIdAndSubjectId(classId, subjectId);
        log.info("Subject {} removed from class {}", subjectId, classId);
    }

    @Override
    @Transactional
    public void removeAllSubjectsFromClass(Long classId) {
        log.info("Removing all subjects from class {}", classId);
        assignmentRepository.deleteByClassId(classId);
    }

    @Override
    @Transactional
    public List<InstituteSubjectDto> getSubjectsByClass(Long classId) {
        log.info("Getting subjects for class {}", classId);

        List<InstituteSubject> subjects = assignmentRepository.findActiveSubjectsByClassId(classId);
        return subjects.stream().map(this::convertSubjectToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<ClassSubjectAssignmentDto> getAssignmentsByClass(Long classId) {
        log.info("Getting assignments for class {}", classId);

        List<ClassSubjectAssignment> assignments = assignmentRepository.findByInstituteClass_IdAndIsActiveTrue(classId);
        return assignments.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean isSubjectAssignedToClass(Long classId, Long subjectId) {
        return assignmentRepository.existsByInstituteClass_IdAndSubject_Id(classId, subjectId);
    }

    // ========== Helper methods ==========

    private ClassSubjectAssignmentDto convertToDto(ClassSubjectAssignment assignment) {
        return ClassSubjectAssignmentDto.builder()
                .id(assignment.getId())
                .classId(assignment.getInstituteClass().getId())
                .className(assignment.getInstituteClass().getClassName())
                .subjectId(assignment.getSubject().getId())
                .subjectName(assignment.getSubject().getSubjectName())
                .subjectCode(assignment.getSubject().getSubjectCode())
                .isActive(assignment.isActive())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }

    private InstituteSubjectDto convertSubjectToDto(InstituteSubject subject) {
        return InstituteSubjectDto.builder()
                .id(subject.getId())
                .subjectName(subject.getSubjectName())
                .subjectCode(subject.getSubjectCode())
                .description(subject.getDescription())
                .isActive(subject.isActive())
                .createdAt(subject.getCreatedAt())
                .updatedAt(subject.getUpdatedAt())
                .build();
    }
}