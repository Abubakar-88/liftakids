package org.liftakids.controller.instituteManage;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.liftakids.dto.InstitutionManage.admission.BulkStudentAdmissionRequest;
import org.liftakids.dto.InstitutionManage.admission.StudentAdmissionRequest;
import org.liftakids.dto.InstitutionManage.admission.StudentSearchResponse;
import org.liftakids.service.instituteManage.StudentAdmissionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/institution/students/admission")
@RequiredArgsConstructor
public class StudentAdmissionController {

    private final StudentAdmissionService admissionService;

    // ========== Single Admission ==========
    @PostMapping("/single")
    public ResponseEntity<StudentSearchResponse> admitSingle(
            @Valid @RequestBody StudentAdmissionRequest request,
            @RequestParam Long institutionId) {
        log.info("POST /api/institution/students/admission/single");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(admissionService.admitStudent(request, institutionId));
    }

    // ========== Bulk Admission ==========
    @PostMapping("/bulk")
    public ResponseEntity<List<StudentSearchResponse>> admitBulk(
            @Valid @RequestBody BulkStudentAdmissionRequest request,
            @RequestParam Long institutionId) {
        log.info("POST /api/institution/students/admission/bulk - {} students", request.getStudentIds().size());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(admissionService.admitStudents(request, institutionId));
    }

    // ========== Search Students (for admission) ==========
    @GetMapping("/search")
    public ResponseEntity<Page<StudentSearchResponse>> searchStudents(
            @RequestParam(required = false) String keyword,
            @RequestParam Long institutionId,
            @RequestParam(required = false) Boolean unassignedOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("GET /api/institution/students/admission/search");
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(admissionService.searchStudentsForAdmit(keyword, institutionId, unassignedOnly, pageable));
    }

    // ========== Get Students by Class ==========
    @GetMapping("/class/{classId}")
    public ResponseEntity<List<StudentSearchResponse>> getStudentsByClass(
            @PathVariable Long classId,
            @RequestParam Long institutionId) {
        log.info("GET /api/institution/students/admission/class/{}", classId);
        return ResponseEntity.ok(admissionService.getStudentsByClass(classId, institutionId));
    }

    // ========== Remove from Class ==========
    @DeleteMapping("/{studentId}")
    public ResponseEntity<Void> removeFromClass(
            @PathVariable Long studentId,
            @RequestParam Long institutionId) {
        log.info("DELETE /api/institution/students/admission/{}", studentId);
        admissionService.removeStudentFromClass(studentId, institutionId);
        return ResponseEntity.noContent().build();
    }
}
