package org.liftakids.service.instituteManage.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.liftakids.dto.student.StudentResponseDto;
import org.liftakids.dto.teacher.*;
import org.liftakids.entity.InstituteManage.*;
import org.liftakids.entity.Institutions;
import org.liftakids.exception.ResourceNotFoundException;
import org.liftakids.exception.UnauthorizedException;
import org.liftakids.repositories.InstitutionRepository;
import org.liftakids.repositories.instituteManage.*;
import org.liftakids.service.instituteManage.TeacherService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;
    private final TeacherClassAssignmentRepository assignmentRepository;
    private final TeacherScheduleRepository scheduleRepository; // 👈 যোগ করুন
    private final InstitutionRepository institutionRepository;
    private final InstituteClassRepository instituteClassRepository;
    private final InstituteSubjectRepository instituteSubjectRepository;


    // Valid Days
    private static final List<String> VALID_DAYS = Arrays.asList(
            "Saturday", "Sunday", "Monday", "Tuesday",
            "Wednesday", "Thursday"
    );

    // ============= CRUD Operations =============

    @Override
    @Transactional
    public TeacherResponseDto createTeacher(TeacherRequestDto requestDto, Long institutionId) {
        log.info("Creating teacher for institution: {}", institutionId);

        Institutions institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found with id: " + institutionId));

        if (teacherRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new RuntimeException("Teacher with email '" + requestDto.getEmail() + "' already exists");
        }

        Teacher teacher = Teacher.builder()
                .name(requestDto.getName())
                .email(requestDto.getEmail())
                .phone(requestDto.getPhone())
                .address(requestDto.getAddress())
                .photoUrl(requestDto.getPhotoUrl())
                .gender(requestDto.getGender())
                .dateOfBirth(requestDto.getDateOfBirth())
                .joiningDate(requestDto.getJoiningDate() != null ? requestDto.getJoiningDate() : LocalDate.now())
                .designation(requestDto.getDesignation())
                .department(requestDto.getDepartment())
                .specialization(requestDto.getSpecialization())
                .emergencyContact(requestDto.getEmergencyContact())
                .institution(institution)
                .active(true)
                .onLeave(false)
                .build();

        String teacherId = generateTeacherId(institutionId);
        teacher.setTeacherId(teacherId);

        // Save Qualifications
        if (requestDto.getQualifications() != null && !requestDto.getQualifications().isEmpty()) {
            List<TeacherQualification> qualifications = requestDto.getQualifications().stream()
                    .map(q -> {
                        TeacherQualification qualification = new TeacherQualification();
                        qualification.setTeacher(teacher);
                        qualification.setDegree(q.getDegree());
                        qualification.setInstitution(q.getInstitution());
                        qualification.setYear(q.getYear());
                        qualification.setGrade(q.getGrade());
                        qualification.setHighest(q.getIsHighest() != null ? q.getIsHighest() : false);
                        qualification.setDescription(q.getDescription());
                        return qualification;
                    })
                    .collect(Collectors.toList());
            teacher.setQualifications(qualifications);
        }

        // Save Experiences
        if (requestDto.getExperiences() != null && !requestDto.getExperiences().isEmpty()) {
            List<TeacherExperience> experiences = requestDto.getExperiences().stream()
                    .map(e -> {
                        TeacherExperience experience = new TeacherExperience();
                        experience.setTeacher(teacher);
                        experience.setInstitution(e.getInstitution());
                        experience.setPosition(e.getPosition());
                        experience.setFromDate(e.getFromDate());
                        experience.setToDate(e.getToDate());
                        experience.setCurrent(e.getIsCurrent() != null ? e.getIsCurrent() : false);
                        experience.setResponsibilities(e.getResponsibilities());
                        return experience;
                    })
                    .collect(Collectors.toList());
            teacher.setExperiences(experiences);
        }

        Teacher savedTeacher = teacherRepository.save(teacher);
        log.info("✅ Teacher created successfully with ID: {} and Teacher ID: {}",
                savedTeacher.getId(), savedTeacher.getTeacherId());

        return convertToDto(savedTeacher);
    }

    @Override
    @Transactional
    public TeacherResponseDto getTeacherById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));
        return convertToDto(teacher);
    }

    @Override
    @Transactional
    public TeacherResponseDto getTeacherByTeacherId(String teacherId) {
        Teacher teacher = teacherRepository.findByTeacherId(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + teacherId));
        return convertToDto(teacher);
    }

    @Override
    @Transactional
    public Page<TeacherResponseDto> getAllTeachers(Long institutionId, Pageable pageable) {
        return teacherRepository.findByInstitution_InstitutionsId(institutionId, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional
    public List<TeacherResponseDto> getAllTeachersList(Long institutionId) {
        return teacherRepository.findByInstitution_InstitutionsId(institutionId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TeacherResponseDto updateTeacher(Long id, TeacherUpdateDto updateDto, Long institutionId) {
        log.info("Updating teacher: {} for institution: {}", id, institutionId);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        if (!teacher.getInstitution().getInstitutionsId().equals(institutionId)) {
            throw new UnauthorizedException("You are not authorized to update this teacher");
        }

        if (updateDto.getName() != null) teacher.setName(updateDto.getName());
        if (updateDto.getEmail() != null) teacher.setEmail(updateDto.getEmail());
        if (updateDto.getPhone() != null) teacher.setPhone(updateDto.getPhone());
        if (updateDto.getAddress() != null) teacher.setAddress(updateDto.getAddress());
        if (updateDto.getPhotoUrl() != null) teacher.setPhotoUrl(updateDto.getPhotoUrl());
        if (updateDto.getGender() != null) teacher.setGender(updateDto.getGender());
        if (updateDto.getDateOfBirth() != null) teacher.setDateOfBirth(updateDto.getDateOfBirth());
        if (updateDto.getDesignation() != null) teacher.setDesignation(updateDto.getDesignation());
        if (updateDto.getDepartment() != null) teacher.setDepartment(updateDto.getDepartment());
        if (updateDto.getSpecialization() != null) teacher.setSpecialization(updateDto.getSpecialization());
        if (updateDto.getEmergencyContact() != null) teacher.setEmergencyContact(updateDto.getEmergencyContact());
        if (updateDto.getActive() != null) teacher.setActive(updateDto.getActive());
        if (updateDto.getOnLeave() != null) {
            teacher.setOnLeave(updateDto.getOnLeave());
            if (updateDto.getOnLeave()) {
                teacher.setLeaveReason(updateDto.getLeaveReason());
            }
        }

        Teacher updatedTeacher = teacherRepository.save(teacher);
        return convertToDto(updatedTeacher);
    }

    @Override
    @Transactional
    public void deleteTeacher(Long id, Long institutionId) {
        log.info("Deleting teacher: {} for institution: {}", id, institutionId);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        if (!teacher.getInstitution().getInstitutionsId().equals(institutionId)) {
            throw new UnauthorizedException("You are not authorized to delete this teacher");
        }

        teacherRepository.delete(teacher);
        log.info("Teacher deleted: {}", id);
    }

    // ============= Search =============

    @Override
    @Transactional
    public Page<TeacherResponseDto> searchTeachers(Long institutionId, String keyword, Pageable pageable) {
        return teacherRepository.searchTeachers(institutionId, keyword, pageable)
                .map(this::convertToDto);
    }

    // ============= Student Management =============

    @Override
    @Transactional
    public List<StudentResponseDto> getStudentsByTeacher(Long teacherId) {
        return new ArrayList<>();
    }

    // ============= Academic =============

    @Override
    @Transactional
    public TeacherResponseDto enterGrades(Long teacherId, GradeEntryDto gradeEntryDto) {
        return getTeacherById(teacherId);
    }

    @Override
    @Transactional
    public TeacherPerformanceDto getTeacherPerformance(Long teacherId) {
        return null;
    }

    @Override
    @Transactional
    public TeacherAttendanceDto getTeacherAttendance(Long teacherId) {
        return null;
    }

    // ============= Communication =============

    @Override
    @Transactional
    public void sendMessageToStudents(Long teacherId, MessageDto messageDto) {
        log.info("Sending message to students from teacher: {}", teacherId);
    }

    @Override
    @Transactional
    public void sendMessageToParents(Long teacherId, MessageDto messageDto) {
        log.info("Sending message to parents from teacher: {}", teacherId);
    }

    @Override
    public List<NoticeDto> getTeacherNotices(Long teacherId) {
        return new ArrayList<>();
    }

    // ============= Statistics =============

    @Override
    @Transactional
    public Map<String, Object> getTeacherStatistics(Long institutionId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", teacherRepository.countTotalTeachers(institutionId));
        stats.put("active", teacherRepository.countActiveTeachers(institutionId));
        stats.put("onLeave", teacherRepository.countTeachersOnLeave(institutionId));
        return stats;
    }

    @Override
    @Transactional
    public TeacherStatsDto getTeacherStats(Long teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        return TeacherStatsDto.builder()
                .totalStudents(0)
                .totalClasses(teacher.getClassAssignments().size())
                .totalSubjects(teacher.getClassAssignments().stream()
                        .map(TeacherClassAssignment::getSubject).distinct().count())
                .attendancePercentage(0.0)
                .build();
    }

    // ============= Class Assignment Methods =============

    @Override
    @Transactional
    public TeacherResponseDto assignClass(Long teacherId, ClassAssignmentRequestDto requestDto) {
        log.info("Assigning class {} to teacher: {}", requestDto.getClassName(), teacherId);

        // ✅ Institution-Specific Validation
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));
        Long institutionId = teacher.getInstitution().getInstitutionsId();

        validateClassName(requestDto.getClassName(), institutionId);
        validateSubject(requestDto.getSubject(), institutionId);

        Optional<TeacherClassAssignment> existing = assignmentRepository
                .findByTeacher_IdAndClassNameAndSubject(
                        teacherId,
                        requestDto.getClassName(),
                        requestDto.getSubject()
                );

        if (existing.isPresent()) {
            throw new RuntimeException("Teacher already assigned to this class and subject");
        }

        TeacherClassAssignment assignment = TeacherClassAssignment.builder()
                .teacher(teacher)
                .className(requestDto.getClassName())
                .section(requestDto.getSection())
                .subject(requestDto.getSubject())
                .isClassTeacher(requestDto.getIsClassTeacher() != null ? requestDto.getIsClassTeacher() : false)
                .academicYear(requestDto.getAcademicYear())
                .isActive(true)
                .build();

        assignmentRepository.save(assignment);

        if (assignment.isClassTeacher()) {
            removePreviousClassTeacher(requestDto.getClassName());
        }

        log.info("Class assigned successfully to teacher: {}", teacher.getName());
        return getTeacherById(teacherId);
    }

    @Override
    @Transactional
    public TeacherResponseDto removeClassAssignment(Long assignmentId) {
        TeacherClassAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

        Teacher teacher = assignment.getTeacher();
        assignmentRepository.delete(assignment);

        log.info("Class assignment removed for teacher: {}", teacher.getName());
        return getTeacherById(teacher.getId());
    }

    @Override
    @Transactional
    public TeacherResponseDto updateClassAssignments(Long teacherId, List<ClassAssignmentRequestDto> requests, Long institutionId) {
        log.info("Updating all class assignments for teacher: {}", teacherId);

        List<TeacherClassAssignment> existingAssignments = assignmentRepository.findByTeacher_Id(teacherId);
        assignmentRepository.deleteAll(existingAssignments);

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        for (ClassAssignmentRequestDto request : requests) {
            validateClassName(request.getClassName(), institutionId);

            TeacherClassAssignment assignment = TeacherClassAssignment.builder()
                    .teacher(teacher)
                    .className(request.getClassName())
                    .section(request.getSection())
                    .subject(request.getSubject())
                    .isClassTeacher(request.getIsClassTeacher() != null ? request.getIsClassTeacher() : false)
                    .academicYear(request.getAcademicYear())
                    .isActive(true)
                    .build();

            assignmentRepository.save(assignment);
        }

        return getTeacherById(teacherId);
    }

    @Override
    @Transactional
    public List<ClassAssignmentResponseDto> getTeacherClassAssignments(Long teacherId) {
        List<TeacherClassAssignment> assignments = assignmentRepository
                .findByTeacher_IdAndIsActiveTrue(teacherId);

        return assignments.stream()
                .map(this::convertToAssignmentDto)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public List<TeacherResponseDto> getTeachersByClass(String className, Long institutionId) {
        validateClassName(className, institutionId);

        List<TeacherClassAssignment> assignments = assignmentRepository
                .findByClassName(className);

        return assignments.stream()
                .map(TeacherClassAssignment::getTeacher)
                .distinct()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ============= Schedule Methods =============

    @Override
    @Transactional
    public TeacherResponseDto updateSchedule(Long teacherId, ScheduleRequestDto requestDto, Long institutionId) {
        log.info("Updating schedule for teacher: {} in institution: {}", teacherId, institutionId);

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        if (!teacher.getInstitution().getInstitutionsId().equals(institutionId)) {
            throw new UnauthorizedException("You are not authorized to update this teacher's schedule");
        }

        scheduleRepository.deleteByTeacher_Id(teacherId);

        for (ScheduleItemDto item : requestDto.getSchedules()) {
            // ✅ Institution-Specific Validation
            validateClassName(item.getClassName(), institutionId);
            validateSubject(item.getSubject(), institutionId);

            TeacherSchedule schedule = TeacherSchedule.builder()
                    .teacher(teacher)
                    .className(item.getClassName())
                    .subject(item.getSubject())
                    .day(item.getDay())
                    .startTime(LocalTime.parse(item.getStartTime()))
                    .endTime(LocalTime.parse(item.getEndTime()))
                    .room(item.getRoom())
                    .isActive(true)
                    .build();

            scheduleRepository.save(schedule);
        }

        return getTeacherById(teacherId);
    }

    // ============= Validation Helper Methods =============

    private void validateClassName(String className, Long institutionId) {
        boolean exists = instituteClassRepository.existsByInstitution_InstitutionsIdAndClassName(
                institutionId, className);   // section null ধরে নিচ্ছি
        if (!exists) {
            throw new IllegalArgumentException("Invalid class name: " + className +
                    " for institution " + institutionId);
        }
    }

    private void validateSubject(String subjectName, Long institutionId) {
        boolean exists = instituteSubjectRepository.existsByInstitution_InstitutionsIdAndSubjectName(
                institutionId, subjectName);
        if (!exists) {
            throw new IllegalArgumentException("Invalid subject: " + subjectName +
                    " for institution " + institutionId);
        }
    }
    @Override
    @Transactional
    public List<ScheduleResponseDto> getTeacherSchedule(Long teacherId) {
        List<TeacherSchedule> schedules = scheduleRepository
                .findByTeacher_IdAndIsActiveTrue(teacherId);

        return schedules.stream()
                .map(this::convertToScheduleDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<ScheduleResponseDto> getTeacherScheduleByDay(Long teacherId, String day) {
        List<TeacherSchedule> schedules = scheduleRepository
                .findByTeacher_IdAndDay(teacherId, day);

        return schedules.stream()
                .map(this::convertToScheduleDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TeacherResponseDto updateDaySchedule(Long teacherId, String day, List<ScheduleItemDto> daySchedules, Long institutionId) {
        log.info("Updating {} schedule for teacher: {}", day, teacherId);

        scheduleRepository.deleteByTeacher_IdAndDay(teacherId, day);

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        for (ScheduleItemDto item : daySchedules) {
            validateClassName(item.getClassName(),institutionId);

            TeacherSchedule schedule = TeacherSchedule.builder()
                    .teacher(teacher)
                    .className(item.getClassName())
                    .subject(item.getSubject())
                    .day(day)
                    .startTime(LocalTime.parse(item.getStartTime()))
                    .endTime(LocalTime.parse(item.getEndTime()))
                    .room(item.getRoom())
                    .isActive(true)
                    .build();

            scheduleRepository.save(schedule);
        }

        return getTeacherById(teacherId);
    }

    // ============= Helper Methods =============

    private String generateTeacherId(Long institutionId) {
        int year = LocalDate.now().getYear();
        long count = teacherRepository.countTotalTeachers(institutionId) + 1;
        return String.format("TCH-%d-%03d", year, count);
    }

    // ============= Valid Values Methods =============
    // এখন আর Static List না, ডাটাবেস থেকে ফেরত দেব

    @Override
    @Transactional
    public List<String> getValidClasses(Long institutionId) {
        return instituteClassRepository.findByInstitution_InstitutionsIdAndIsActiveTrueOrderByClassOrderAsc(institutionId)
                .stream().map(InstituteClass::getClassName).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<String> getValidSubjects(Long institutionId) {
        return instituteSubjectRepository.findByInstitution_InstitutionsIdAndIsActiveTrueOrderBySubjectNameAsc(institutionId)
                .stream().map(InstituteSubject::getSubjectName).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<String> getValidDays() {
        return Arrays.asList("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday");
    }

    private void removePreviousClassTeacher(String className) {
        List<TeacherClassAssignment> assignments = assignmentRepository.findByClassName(className);
        for (TeacherClassAssignment assignment : assignments) {
            if (assignment.isClassTeacher()) {
                assignment.setClassTeacher(false);
                assignmentRepository.save(assignment);
            }
        }
    }

    // ============= Convert Methods =============

    private TeacherResponseDto convertToDto(Teacher teacher) {
        if (teacher == null) return null;

        return TeacherResponseDto.builder()
                .id(teacher.getId())
                .teacherId(teacher.getTeacherId())
                .name(teacher.getName())
                .email(teacher.getEmail())
                .phone(teacher.getPhone())
                .address(teacher.getAddress())
                .photoUrl(teacher.getPhotoUrl())
                .gender(teacher.getGender())
                .dateOfBirth(teacher.getDateOfBirth())
                .joiningDate(teacher.getJoiningDate())
                .designation(teacher.getDesignation())
                .department(teacher.getDepartment())
                .specialization(teacher.getSpecialization())
                .active(teacher.isActive())
                .onLeave(teacher.isOnLeave())
                .leaveReason(teacher.getLeaveReason())
                .emergencyContact(teacher.getEmergencyContact())
                .institutionId(teacher.getInstitution().getInstitutionsId())
                .institutionName(teacher.getInstitution().getInstitutionName())
                .qualifications(convertQualifications(teacher.getQualifications()))
                .experiences(convertExperiences(teacher.getExperiences()))
                .classAssignments(convertAssignments(teacher.getClassAssignments()))
                .schedules(convertSchedules(teacher.getSchedules()))
                .createdAt(teacher.getCreatedAt())
                .updatedAt(teacher.getUpdatedAt())
                .build();
    }

    private List<QualificationResponseDto> convertQualifications(List<TeacherQualification> qualifications) {
        if (qualifications == null) return new ArrayList<>();
        return qualifications.stream()
                .map(q -> QualificationResponseDto.builder()
                        .id(q.getId())
                        .degree(q.getDegree())
                        .institution(q.getInstitution())
                        .year(q.getYear())
                        .grade(q.getGrade())
                        .isHighest(q.isHighest())
                        .description(q.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    private List<ExperienceResponseDto> convertExperiences(List<TeacherExperience> experiences) {
        if (experiences == null) return new ArrayList<>();
        return experiences.stream()
                .map(e -> ExperienceResponseDto.builder()
                        .id(e.getId())
                        .institution(e.getInstitution())
                        .position(e.getPosition())
                        .fromDate(e.getFromDate())
                        .toDate(e.getToDate())
                        .isCurrent(e.isCurrent())
                        .responsibilities(e.getResponsibilities())
                        .build())
                .collect(Collectors.toList());
    }

    private List<ClassAssignmentResponseDto> convertAssignments(List<TeacherClassAssignment> assignments) {
        if (assignments == null) return new ArrayList<>();
        return assignments.stream()
                .map(this::convertToAssignmentDto)
                .collect(Collectors.toList());
    }

    private List<ScheduleResponseDto> convertSchedules(List<TeacherSchedule> schedules) {
        if (schedules == null) return new ArrayList<>();
        return schedules.stream()
                .map(this::convertToScheduleDto)
                .collect(Collectors.toList());
    }

    private ClassAssignmentResponseDto convertToAssignmentDto(TeacherClassAssignment assignment) {
        return ClassAssignmentResponseDto.builder()
                .id(assignment.getId())
                .teacherId(assignment.getTeacher().getId())
                .teacherName(assignment.getTeacher().getName())
                .className(assignment.getClassName())
                .section(assignment.getSection())
                .subject(assignment.getSubject())
                .isClassTeacher(assignment.isClassTeacher())
                .academicYear(assignment.getAcademicYear())
                .isActive(assignment.isActive())
                .assignedAt(assignment.getAssignedAt() != null ?
                        assignment.getAssignedAt().toString() : null)
                .build();
    }

    private ScheduleResponseDto convertToScheduleDto(TeacherSchedule schedule) {
        return ScheduleResponseDto.builder()
                .id(schedule.getId())
                .teacherId(schedule.getTeacher().getId())
                .teacherName(schedule.getTeacher().getName())
                .className(schedule.getClassName())
                .subject(schedule.getSubject())
                .day(schedule.getDay())
                .startTime(schedule.getStartTime() != null ?
                        schedule.getStartTime().toString() : null)
                .endTime(schedule.getEndTime() != null ?
                        schedule.getEndTime().toString() : null)
                .room(schedule.getRoom())
                .isActive(schedule.isActive())
                .build();
    }


}