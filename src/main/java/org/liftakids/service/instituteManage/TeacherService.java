package org.liftakids.service.instituteManage;

import org.liftakids.dto.student.StudentResponseDto;
import org.liftakids.dto.teacher.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface TeacherService {

    // ============= CRUD Operations =============
    TeacherResponseDto createTeacher(TeacherRequestDto requestDto, Long institutionId);
    TeacherResponseDto getTeacherById(Long id);
    TeacherResponseDto getTeacherByTeacherId(String teacherId);
    Page<TeacherResponseDto> getAllTeachers(Long institutionId, Pageable pageable);
    List<TeacherResponseDto> getAllTeachersList(Long institutionId);
    TeacherResponseDto updateTeacher(Long id, TeacherUpdateDto updateDto, Long institutionId);
    void deleteTeacher(Long id, Long institutionId);

    // ============= Search =============
    Page<TeacherResponseDto> searchTeachers(Long institutionId, String keyword, Pageable pageable);

    // ============= Class Assignment =============
    TeacherResponseDto assignClass(Long teacherId, ClassAssignmentRequestDto requestDto);
    TeacherResponseDto removeClassAssignment(Long assignmentId);
    TeacherResponseDto updateClassAssignments(Long teacherId, List<ClassAssignmentRequestDto> requests, Long institutionId);
    List<ClassAssignmentResponseDto> getTeacherClassAssignments(Long teacherId);
    List<TeacherResponseDto> getTeachersByClass(String className, Long institutionId);
    // ============= Schedule Management =============
    TeacherResponseDto updateSchedule(Long teacherId, ScheduleRequestDto requestDto, Long institutionId);
    List<ScheduleResponseDto> getTeacherSchedule(Long teacherId);
    List<ScheduleResponseDto> getTeacherScheduleByDay(Long teacherId, String day);
    TeacherResponseDto updateDaySchedule(Long teacherId, String day, List<ScheduleItemDto> daySchedules, Long institutionId);

    // ============= Student Management =============
    List<StudentResponseDto> getStudentsByTeacher(Long teacherId);

    // ============= Academic =============
    TeacherResponseDto enterGrades(Long teacherId, GradeEntryDto gradeEntryDto);
    TeacherPerformanceDto getTeacherPerformance(Long teacherId);
    TeacherAttendanceDto getTeacherAttendance(Long teacherId);

    // ============= Communication =============
    void sendMessageToStudents(Long teacherId, MessageDto messageDto);
    void sendMessageToParents(Long teacherId, MessageDto messageDto);
    List<NoticeDto> getTeacherNotices(Long teacherId);

    // ============= Statistics =============
    Map<String, Object> getTeacherStatistics(Long institutionId);
    TeacherStatsDto getTeacherStats(Long teacherId);

    // ============= 🔥 Institution-Specific Valid Values =============
    List<String> getValidClasses(Long institutionId);     // 👈 এখন institutionId নেবে
    List<String> getValidSubjects(Long institutionId);    // 👈 এখন institutionId নেবে
    List<String> getValidDays();                          // 👈 এটা গ্লোবাল
}