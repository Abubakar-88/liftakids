package org.liftakids.controller.instituteManage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.liftakids.dto.InstitutionManage.InstituteSubjectDto;
import org.liftakids.service.instituteManage.InstituteSubjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/institution/subjects")
@RequiredArgsConstructor

public class InstituteSubjectController {

    private final InstituteSubjectService subjectService;

    @PostMapping
    public ResponseEntity<InstituteSubjectDto> createSubject(
            @RequestBody InstituteSubjectDto requestDto,
            @RequestParam Long institutionId) {
        log.info("POST /api/institution/subjects");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subjectService.createSubject(requestDto, institutionId));
    }

    @GetMapping
    public ResponseEntity<List<InstituteSubjectDto>> getAllSubjects(
            @RequestParam Long institutionId) {
        log.info("GET /api/institution/subjects");
        return ResponseEntity.ok(subjectService.getAllSubjects(institutionId));
    }

    @GetMapping("/active")
    public ResponseEntity<List<InstituteSubjectDto>> getActiveSubjects(
            @RequestParam Long institutionId) {
        log.info("GET /api/institution/subjects/active");
        return ResponseEntity.ok(subjectService.getActiveSubjects(institutionId));
    }

    @GetMapping("/{subjectId}")
    public ResponseEntity<InstituteSubjectDto> getSubjectById(@PathVariable Long subjectId) {
        log.info("GET /api/institution/subjects/{}", subjectId);
        return ResponseEntity.ok(subjectService.getSubjectById(subjectId));
    }

    @PutMapping("/{subjectId}")
    public ResponseEntity<InstituteSubjectDto> updateSubject(
            @PathVariable Long subjectId,
            @RequestBody InstituteSubjectDto requestDto,
            @RequestParam Long institutionId) {
        log.info("PUT /api/institution/subjects/{}", subjectId);
        return ResponseEntity.ok(subjectService.updateSubject(subjectId, requestDto, institutionId));
    }

    @DeleteMapping("/{subjectId}")
    public ResponseEntity<Void> deleteSubject(
            @PathVariable Long subjectId,
            @RequestParam Long institutionId) {
        log.info("DELETE /api/institution/subjects/{}", subjectId);
        subjectService.deleteSubject(subjectId, institutionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<InstituteSubjectDto>> createMultipleSubjects(
            @RequestBody List<InstituteSubjectDto> subjectDtos,
            @RequestParam Long institutionId) {
        log.info("POST /api/institution/subjects/bulk");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subjectService.createMultipleSubjects(subjectDtos, institutionId));
    }

    @PostMapping("/setup-default")
    public ResponseEntity<Void> setupDefaultSubjects(
            @RequestParam Long institutionId) {
        log.info("POST /api/institution/subjects/setup-default");
        subjectService.setupDefaultSubjectsForInstitution(institutionId);
        return ResponseEntity.ok().build();
    }
}