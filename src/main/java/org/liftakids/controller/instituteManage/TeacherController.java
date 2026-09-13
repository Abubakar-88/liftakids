package org.liftakids.controller.instituteManage;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.liftakids.dto.student.StudentResponseDto;
import org.liftakids.dto.teacher.*;
import org.liftakids.service.instituteManage.TeacherService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/institution/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    // ========================================
    // ============= CRUD Operations =============
    // ========================================

    @PostMapping
    public ResponseEntity<TeacherResponseDto> createTeacher(
            @Valid @RequestBody TeacherRequestDto requestDto,
            @RequestParam Long institutionId) {
        log.info("POST /api/institution/teachers - Creating teacher");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(teacherService.createTeacher(requestDto, institutionId));
    }

    @GetMapping
    public ResponseEntity<Page<TeacherResponseDto>> getAllTeachers(
            @RequestParam Long institutionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        log.info("GET /api/institution/teachers - Getting all teachers");
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(teacherService.getAllTeachers(institutionId, pageable));
    }

    @GetMapping("/list")
    public ResponseEntity<List<TeacherResponseDto>> getAllTeachersList(
            @RequestParam Long institutionId) {
        log.info("GET /api/institution/teachers/list - Getting all teachers list");
        return ResponseEntity.ok(teacherService.getAllTeachersList(institutionId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeacherResponseDto> getTeacherById(@PathVariable Long id) {
        log.info("GET /api/institution/teachers/{}", id);
        return ResponseEntity.ok(teacherService.getTeacherById(id));
    }

    @GetMapping("/teacher-id/{teacherId}")
    public ResponseEntity<TeacherResponseDto> getTeacherByTeacherId(@PathVariable String teacherId) {
        log.info("GET /api/institution/teachers/teacher-id/{}", teacherId);
        return ResponseEntity.ok(teacherService.getTeacherByTeacherId(teacherId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeacherResponseDto> updateTeacher(
            @PathVariable Long id,
            @Valid @RequestBody TeacherUpdateDto updateDto,
            @RequestParam Long institutionId) {

        log.info("PUT /api/institution/teachers/{}", id);
        return ResponseEntity.ok(teacherService.updateTeacher(id, updateDto, institutionId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeacher(
            @PathVariable Long id,
            @RequestParam Long institutionId) {

        log.info("DELETE /api/institution/teachers/{}", id);
        teacherService.deleteTeacher(id, institutionId);
        return ResponseEntity.noContent().build();
    }

    // ============= Search =============

    @GetMapping("/search")
    public ResponseEntity<Page<TeacherResponseDto>> searchTeachers(
            @RequestParam Long institutionId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("GET /api/institution/teachers/search - Keyword: {}", keyword);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(teacherService.searchTeachers(institutionId, keyword, pageable));
    }

    // ========================================
    // ============= Class Assignment =============
    // ========================================

    @PostMapping("/{id}/assign-class")
    public ResponseEntity<TeacherResponseDto> assignClass(
            @PathVariable Long id,
            @Valid @RequestBody ClassAssignmentRequestDto requestDto) {

        log.info("POST /api/institution/teachers/{}/assign-class", id);
        return ResponseEntity.ok(teacherService.assignClass(id, requestDto));
    }

    @DeleteMapping("/assignments/{assignmentId}")
    public ResponseEntity<TeacherResponseDto> removeClassAssignment(
            @PathVariable Long assignmentId) {

        log.info("DELETE /api/institution/teachers/assignments/{}", assignmentId);
        return ResponseEntity.ok(teacherService.removeClassAssignment(assignmentId));
    }

    @PutMapping("/{id}/assignments")
    public ResponseEntity<TeacherResponseDto> updateClassAssignments(
            @PathVariable Long id,
            @Valid @RequestBody List<ClassAssignmentRequestDto> requests,
            @RequestParam Long institutionId) {   // ✅ institutionId added

        log.info("PUT /api/institution/teachers/{}/assignments", id);
        return ResponseEntity.ok(teacherService.updateClassAssignments(id, requests, institutionId));
    }

    @GetMapping("/{id}/assignments")
    public ResponseEntity<List<ClassAssignmentResponseDto>> getTeacherClassAssignments(
            @PathVariable Long id) {

        log.info("GET /api/institution/teachers/{}/assignments", id);
        return ResponseEntity.ok(teacherService.getTeacherClassAssignments(id));
    }

    @GetMapping("/by-class/{className}")
    public ResponseEntity<List<TeacherResponseDto>> getTeachersByClass(
            @PathVariable String className,
            @RequestParam Long institutionId) {   // ✅ institutionId added

        log.info("GET /api/institution/teachers/by-class/{}", className);
        return ResponseEntity.ok(teacherService.getTeachersByClass(className, institutionId));
    }

    // ========================================
    // ============= Schedule Management =============
    // ========================================

    @PutMapping("/{id}/schedule")
    public ResponseEntity<TeacherResponseDto> updateSchedule(
            @PathVariable Long id,
            @RequestBody ScheduleRequestDto requestDto,
            @RequestParam Long institutionId) {
        log.info("Received schedule update: {}", requestDto);
        log.info("PUT /api/institution/teachers/{}/schedule for institution: {}", id, institutionId);
        return ResponseEntity.ok(teacherService.updateSchedule(id, requestDto, institutionId));
    }

    @GetMapping("/{id}/schedule")
    public ResponseEntity<List<ScheduleResponseDto>> getTeacherSchedule(
            @PathVariable Long id) {

        log.info("GET /api/institution/teachers/{}/schedule", id);
        return ResponseEntity.ok(teacherService.getTeacherSchedule(id));
    }

    @GetMapping("/{id}/schedule/{day}")
    public ResponseEntity<List<ScheduleResponseDto>> getTeacherScheduleByDay(
            @PathVariable Long id,
            @PathVariable String day) {

        log.info("GET /api/institution/teachers/{}/schedule/{}", id, day);
        return ResponseEntity.ok(teacherService.getTeacherScheduleByDay(id, day));
    }

    @PutMapping("/{id}/schedule/{day}")
    public ResponseEntity<TeacherResponseDto> updateDaySchedule(
            @PathVariable Long id,
            @PathVariable String day,
            @Valid @RequestBody List<ScheduleItemDto> daySchedules,
            @RequestParam Long institutionId) {   // ✅ institutionId added

        log.info("PUT /api/institution/teachers/{}/schedule/{}", id, day);
        return ResponseEntity.ok(teacherService.updateDaySchedule(id, day, daySchedules, institutionId));
    }

    // ========================================
    // ============= Student Management =============
    // ========================================

    @GetMapping("/{id}/students")
    public ResponseEntity<List<StudentResponseDto>> getStudentsByTeacher(
            @PathVariable Long id) {

        log.info("GET /api/institution/teachers/{}/students", id);
        return ResponseEntity.ok(teacherService.getStudentsByTeacher(id));
    }

    // ========================================
    // ============= Academic =============
    // ========================================

    @PostMapping("/{id}/enter-grade")
    public ResponseEntity<TeacherResponseDto> enterGrades(
            @PathVariable Long id,
            @Valid @RequestBody GradeEntryDto gradeEntryDto) {

        log.info("POST /api/institution/teachers/{}/enter-grade", id);
        return ResponseEntity.ok(teacherService.enterGrades(id, gradeEntryDto));
    }

    @GetMapping("/{id}/performance")
    public ResponseEntity<TeacherPerformanceDto> getTeacherPerformance(
            @PathVariable Long id) {

        log.info("GET /api/institution/teachers/{}/performance", id);
        return ResponseEntity.ok(teacherService.getTeacherPerformance(id));
    }

    @GetMapping("/{id}/attendance")
    public ResponseEntity<TeacherAttendanceDto> getTeacherAttendance(
            @PathVariable Long id) {

        log.info("GET /api/institution/teachers/{}/attendance", id);
        return ResponseEntity.ok(teacherService.getTeacherAttendance(id));
    }

    // ========================================
    // ============= Communication =============
    // ========================================

    @PostMapping("/{id}/message-students")
    public ResponseEntity<Void> sendMessageToStudents(
            @PathVariable Long id,
            @Valid @RequestBody MessageDto messageDto) {

        log.info("POST /api/institution/teachers/{}/message-students", id);
        teacherService.sendMessageToStudents(id, messageDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/message-parents")
    public ResponseEntity<Void> sendMessageToParents(
            @PathVariable Long id,
            @Valid @RequestBody MessageDto messageDto) {

        log.info("POST /api/institution/teachers/{}/message-parents", id);
        teacherService.sendMessageToParents(id, messageDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/notices")
    public ResponseEntity<List<NoticeDto>> getTeacherNotices(
            @PathVariable Long id) {

        log.info("GET /api/institution/teachers/{}/notices", id);
        return ResponseEntity.ok(teacherService.getTeacherNotices(id));
    }

    // ========================================
    // ============= Statistics =============
    // ========================================

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getTeacherStatistics(
            @RequestParam Long institutionId) {

        log.info("GET /api/institution/teachers/statistics");
        return ResponseEntity.ok(teacherService.getTeacherStatistics(institutionId));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<TeacherStatsDto> getTeacherStats(
            @PathVariable Long id) {

        log.info("GET /api/institution/teachers/{}/stats", id);
        return ResponseEntity.ok(teacherService.getTeacherStats(id));
    }

    // ========================================
    // ============= Valid Values (Dropdown) =============
    // ========================================

    @GetMapping("/valid-classes")
    public ResponseEntity<List<String>> getValidClasses(
            @RequestParam Long institutionId) {   // ✅ institutionId added

        log.info("GET /api/institution/teachers/valid-classes");
        return ResponseEntity.ok(teacherService.getValidClasses(institutionId));
    }

    @GetMapping("/valid-subjects")
    public ResponseEntity<List<String>> getValidSubjects(
            @RequestParam Long institutionId) {   // ✅ institutionId added

        log.info("GET /api/institution/teachers/valid-subjects");
        return ResponseEntity.ok(teacherService.getValidSubjects(institutionId));
    }

    @GetMapping("/valid-days")
    public ResponseEntity<List<String>> getValidDays() {
        log.info("GET /api/institution/teachers/valid-days");
        return ResponseEntity.ok(teacherService.getValidDays());
    }
}