package org.liftakids.controller.instituteManage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.liftakids.dto.InstitutionManage.InstituteClassDto;
import org.liftakids.service.instituteManage.InstituteClassService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/institution/classes")
@RequiredArgsConstructor
public class InstituteClassController {

    private final InstituteClassService classService;

    // ============= CRUD Operations =============

    @PostMapping
    public ResponseEntity<InstituteClassDto> createClass(
            @RequestBody InstituteClassDto requestDto,
            @RequestParam Long institutionId) {
        log.info("POST /api/institution/classes - Creating class");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(classService.createClass(requestDto, institutionId));
    }

    @GetMapping
    public ResponseEntity<List<InstituteClassDto>> getAllClasses(
            @RequestParam Long institutionId) {
        log.info("GET /api/institution/classes - Getting all classes");
        return ResponseEntity.ok(classService.getAllClasses(institutionId));
    }

    @GetMapping("/active")
    public ResponseEntity<List<InstituteClassDto>> getActiveClasses(
            @RequestParam Long institutionId) {
        log.info("GET /api/institution/classes/active");
        return ResponseEntity.ok(classService.getActiveClasses(institutionId));
    }

    @GetMapping("/{classId}")
    public ResponseEntity<InstituteClassDto> getClassById(@PathVariable Long classId) {
        log.info("GET /api/institution/classes/{}", classId);
        return ResponseEntity.ok(classService.getClassById(classId));
    }

    @GetMapping("/{classId}/student-count")
    public ResponseEntity<Long> getStudentCountByClass(@PathVariable Long classId) {
        log.info("GET /api/institution/classes/{}/student-count", classId);
        return ResponseEntity.ok(classService.getStudentCountByClass(classId));
    }

    @PutMapping("/{classId}")
    public ResponseEntity<InstituteClassDto> updateClass(
            @PathVariable Long classId,
            @RequestBody InstituteClassDto requestDto,
            @RequestParam Long institutionId) {
        log.info("PUT /api/institution/classes/{}", classId);
        return ResponseEntity.ok(classService.updateClass(classId, requestDto, institutionId));
    }

    @DeleteMapping("/{classId}")
    public ResponseEntity<Void> deleteClass(
            @PathVariable Long classId,
            @RequestParam Long institutionId) {
        log.info("DELETE /api/institution/classes/{}", classId);
        classService.deleteClass(classId, institutionId);
        return ResponseEntity.noContent().build();
    }

    // ============= Bulk Operations =============

    @PostMapping("/bulk")
    public ResponseEntity<List<InstituteClassDto>> createMultipleClasses(
            @RequestBody List<InstituteClassDto> classDtos,
            @RequestParam Long institutionId) {
        log.info("POST /api/institution/classes/bulk");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(classService.createMultipleClasses(classDtos, institutionId));
    }

    // ============= Default Setup =============

    @PostMapping("/setup-default")
    public ResponseEntity<Void> setupDefaultClasses(
            @RequestParam Long institutionId) {
        log.info("POST /api/institution/classes/setup-default");
        classService.setupDefaultClassesForInstitution(institutionId);
        return ResponseEntity.ok().build();
    }
}