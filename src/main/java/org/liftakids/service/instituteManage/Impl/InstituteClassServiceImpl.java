package org.liftakids.service.instituteManage.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.liftakids.dto.InstitutionManage.InstituteClassDto;
import org.liftakids.dto.InstitutionManage.StudentBasicDto;
import org.liftakids.entity.InstituteManage.InstituteClass;
import org.liftakids.entity.Institutions;
import org.liftakids.entity.Student;
import org.liftakids.exception.ResourceNotFoundException;
import org.liftakids.exception.UnauthorizedException;
import org.liftakids.repositories.InstitutionRepository;
import org.liftakids.repositories.StudentRepository;
import org.liftakids.repositories.instituteManage.InstituteClassRepository;
import org.liftakids.service.instituteManage.InstituteClassService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstituteClassServiceImpl implements InstituteClassService {

    private final InstituteClassRepository classRepository;
    private final InstitutionRepository institutionRepository;
    private final StudentRepository studentRepository;

    private static final List<String> DEFAULT_CLASSES = Arrays.asList(
            "Class 1", "Class 2", "Class 3", "Class 4", "Class 5", "Class 6", "Class 7"
    );

    @Override
    @Transactional
    public InstituteClassDto createClass(InstituteClassDto requestDto, Long institutionId) {
        log.info("Creating class for institution: {}", institutionId);

        Institutions institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found"));

        // Check if class already exists
        if (classRepository.existsByInstitution_InstitutionsIdAndClassName(
                institutionId, requestDto.getClassName())) {
            throw new RuntimeException("Class already exists");
        }

        InstituteClass instituteClass = InstituteClass.builder()
                .className(requestDto.getClassName())
                .section(requestDto.getSection())
                .classOrder(requestDto.getClassOrder())
                .institution(institution)
                .isActive(true)
                .academicYear(requestDto.getAcademicYear())
                .build();

        InstituteClass saved = classRepository.save(instituteClass);
        return convertToDto(saved);
    }

    @Override
    @Transactional
    public List<InstituteClassDto> getAllClasses(Long institutionId) {
        List<InstituteClass> classes = classRepository
                .findByInstitution_InstitutionsIdOrderByClassOrderAsc(institutionId);
        return classes.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<InstituteClassDto> getActiveClasses(Long institutionId) {
        List<InstituteClass> classes = classRepository
                .findByInstitution_InstitutionsIdAndIsActiveTrueOrderByClassOrderAsc(institutionId);
        return classes.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InstituteClassDto getClassById(Long classId) {
        InstituteClass instituteClass = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));
        return convertToDto(instituteClass);
    }

    @Override
    @Transactional
    public long getStudentCountByClass(Long classId) {
        return classRepository.countStudentsByClass(classId);
    }

    @Override
    @Transactional
    public InstituteClassDto updateClass(Long classId, InstituteClassDto requestDto, Long institutionId) {
        log.info("Updating class: {} for institution: {}", classId, institutionId);

        InstituteClass instituteClass = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        if (!instituteClass.getInstitution().getInstitutionsId().equals(institutionId)) {
            throw new UnauthorizedException("You are not authorized to update this class");
        }

        if (requestDto.getClassName() != null) {
            instituteClass.setClassName(requestDto.getClassName());
        }
        if (requestDto.getSection() != null) {
            instituteClass.setSection(requestDto.getSection());
        }
        if (requestDto.getClassOrder() != null) {
            instituteClass.setClassOrder(requestDto.getClassOrder());
        }
        if (requestDto.getIsActive() != null) {
            instituteClass.setActive(requestDto.getIsActive());
        }
        if (requestDto.getAcademicYear() != null) {
            instituteClass.setAcademicYear(requestDto.getAcademicYear());
        }

        InstituteClass updated = classRepository.save(instituteClass);
        return convertToDto(updated);
    }

    @Override
    @Transactional
    public void deleteClass(Long classId, Long institutionId) {
        log.info("Deleting class: {} for institution: {}", classId, institutionId);

        InstituteClass instituteClass = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        if (!instituteClass.getInstitution().getInstitutionsId().equals(institutionId)) {
            throw new UnauthorizedException("You are not authorized to delete this class");
        }

        classRepository.delete(instituteClass);
    }

    @Override
    @Transactional
    public List<InstituteClassDto> createMultipleClasses(List<InstituteClassDto> classDtos, Long institutionId) {
        List<InstituteClassDto> created = new ArrayList<>();
        for (InstituteClassDto dto : classDtos) {
            created.add(createClass(dto, institutionId));
        }
        return created;
    }

    @Override
    @Transactional
    public void setupDefaultClassesForInstitution(Long institutionId) {
        log.info("Setting up default classes for institution: {}", institutionId);

        Institutions institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found"));

        int order = 1;
        for (String className : DEFAULT_CLASSES) {
            if (!classRepository.existsByInstitution_InstitutionsIdAndClassName(
                    institutionId, className)) {
                InstituteClass instituteClass = InstituteClass.builder()
                        .className(className)
                        .classOrder(order)
                        .institution(institution)
                        .isActive(true)
                        .build();
                classRepository.save(instituteClass);
            }
            order++;
        }
    }

    private InstituteClassDto convertToDto(InstituteClass instituteClass) {
        long studentCount = classRepository.countStudentsByClass(instituteClass.getId());

        List<StudentBasicDto> studentDtos = new ArrayList<>();
        if (instituteClass.getStudents() != null) {
            studentDtos = instituteClass.getStudents().stream()
                    .map(this::convertToStudentBasicDto)
                    .collect(Collectors.toList());
        }

        return InstituteClassDto.builder()
                .id(instituteClass.getId())
                .className(instituteClass.getClassName())
                .section(instituteClass.getSection())
                .classOrder(instituteClass.getClassOrder())
                .institutionId(instituteClass.getInstitution().getInstitutionsId())
                .institutionName(instituteClass.getInstitution().getInstitutionName())
                .studentCount((int) studentCount)
                .isActive(instituteClass.isActive())
                .academicYear(instituteClass.getAcademicYear())
                .students(studentDtos)
                .createdAt(instituteClass.getCreatedAt())
                .updatedAt(instituteClass.getUpdatedAt())
                .build();
    }

    private StudentBasicDto convertToStudentBasicDto(Student student) {
        return StudentBasicDto.builder()
                .studentId(student.getStudentId())
                .studentName(student.getStudentName())
                .guardianName(student.getGuardianName())
                .contactNumber(student.getContactNumber())
                .isSponsored(student.isSponsored())
                .build();
    }
}
