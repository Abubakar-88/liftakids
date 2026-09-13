package org.liftakids.controller.instituteManage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.liftakids.dto.InstitutionManage.ClassSubjectAssignmentDto;
import org.liftakids.dto.InstitutionManage.InstituteSubjectDto;
import org.liftakids.service.instituteManage.ClassSubjectAssignmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/institution/class-subjects")
@RequiredArgsConstructor
public class ClassSubjectController {

    private final ClassSubjectAssignmentService assignmentService;

    // ========== Assign subjects to class ==========
    @PostMapping("/class/{classId}/assign")
    public ResponseEntity<List<ClassSubjectAssignmentDto>> assignSubjectsToClass(
            @PathVariable Long classId,
            @RequestBody List<Long> subjectIds) {

        log.info("POST /api/institution/class-subjects/class/{}/assign", classId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(assignmentService.assignSubjectsToClass(classId, subjectIds));
    }

    // ========== Remove subject from class ==========
    @DeleteMapping("/class/{classId}/subject/{subjectId}")
    public ResponseEntity<Void> removeSubjectFromClass(
            @PathVariable Long classId,
            @PathVariable Long subjectId) {

        log.info("DELETE /api/institution/class-subjects/class/{}/subject/{}", classId, subjectId);
        assignmentService.removeSubjectFromClass(classId, subjectId);
        return ResponseEntity.noContent().build();
    }

    // ========== Remove all subjects from class ==========
    @DeleteMapping("/class/{classId}/subjects")
    public ResponseEntity<Void> removeAllSubjectsFromClass(@PathVariable Long classId) {
        log.info("DELETE /api/institution/class-subjects/class/{}/subjects", classId);
        assignmentService.removeAllSubjectsFromClass(classId);
        return ResponseEntity.noContent().build();
    }

    // ========== Get subjects of a class ==========
    @GetMapping("/class/{classId}/subjects")
    public ResponseEntity<List<InstituteSubjectDto>> getSubjectsByClass(@PathVariable Long classId) {
        log.info("GET /api/institution/class-subjects/class/{}/subjects", classId);
        return ResponseEntity.ok(assignmentService.getSubjectsByClass(classId));
    }

    // ========== Get full assignments of a class ==========
    @GetMapping("/class/{classId}/assignments")
    public ResponseEntity<List<ClassSubjectAssignmentDto>> getAssignmentsByClass(@PathVariable Long classId) {
        log.info("GET /api/institution/class-subjects/class/{}/assignments", classId);
        return ResponseEntity.ok(assignmentService.getAssignmentsByClass(classId));
    }

    // ========== Check if subject assigned ==========
    @GetMapping("/class/{classId}/subject/{subjectId}/check")
    public ResponseEntity<Boolean> isSubjectAssigned(
            @PathVariable Long classId,
            @PathVariable Long subjectId) {

        log.info("GET /api/institution/class-subjects/class/{}/subject/{}/check", classId, subjectId);
        return ResponseEntity.ok(assignmentService.isSubjectAssignedToClass(classId, subjectId));
    }
}