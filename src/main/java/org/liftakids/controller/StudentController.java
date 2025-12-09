package org.liftakids.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.liftakids.dto.student.StudentRequestDto;
import org.liftakids.dto.student.StudentResponseDto;
import org.liftakids.dto.student.StudentUpdateRequestDTO;
import org.liftakids.entity.Sponsorship;
import org.liftakids.entity.SponsorshipStatus;
import org.liftakids.entity.Student;
import org.liftakids.exception.ResourceNotFoundException;
import org.liftakids.repositories.SponsorshipRepository;
import org.liftakids.service.StudentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final SponsorshipRepository sponsorshipRepository;
//    @PostMapping("/addStudent")
//    public ResponseEntity<StudentResponseDto> createStudent(@Valid @RequestBody StudentRequestDto requestDto) {
//        return new ResponseEntity<>(studentService.createStudent(requestDto), HttpStatus.CREATED);
//    }
@PostMapping(value = "/addStudent", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<StudentResponseDto> createStudent(
        @RequestPart("studentData") @Valid StudentRequestDto requestDto,
        @RequestPart("image") MultipartFile image) {
    try {
        return new ResponseEntity<>(studentService.createStudent(requestDto, image), HttpStatus.CREATED);
    } catch (IOException e) {
        throw new RuntimeException("Failed to process image", e);
    }
}
//    @PutMapping("/{studentId}")
//    public ResponseEntity<StudentResponseDto> updateStudent(
//            @PathVariable Long studentId,
//            @Valid @RequestBody StudentUpdateRequestDTO updateRequest) {
//
//        StudentResponseDto updatedStudent = studentService.updateStudent(studentId, updateRequest);
//        return ResponseEntity.ok(updatedStudent);
//    }

    @PutMapping(value = "/updateStudent/{studentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StudentResponseDto> updateStudent(
            @PathVariable Long studentId,
            @RequestPart("studentData") @Valid StudentUpdateRequestDTO updateRequest,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        try {
            return new ResponseEntity<>(studentService.updateStudent(studentId, updateRequest, image), HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException("Failed to process image", e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDto> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @GetMapping
    public ResponseEntity<List<StudentResponseDto>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/all")
    public ResponseEntity<Page<StudentResponseDto>> getAllStudentsWithPagination(
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @RequestParam(defaultValue = "studentName,asc") String[] sort // Example: studentName,asc OR dob,desc
    ) {
        // Extract sort field and direction
        String sortBy = sort[0];
        String sortDirection = sort.length > 1 ? sort[1] : "asc";

        Sort sortObj = sortDirection.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortObj);

        Page<StudentResponseDto> students = studentService.getAllStudents(sortedPageable);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/institution/{institutionId}")
    public ResponseEntity<List<StudentResponseDto>> getStudentsByInstitution(@PathVariable Long institutionId) {
        List<StudentResponseDto> students = studentService.getStudentsByInstitution(institutionId);
        return ResponseEntity.ok(students);
    }
    @GetMapping("/institution/{institutionId}/withPagination")
    public ResponseEntity<Page<StudentResponseDto>> getStudentsByInstitutionWithPagination(
            @PathVariable Long institutionId,
            @PageableDefault(
                    size = 10,
                    page = 0,
                    sort = "studentName",
                    direction = Sort.Direction.ASC
            ) Pageable pageable,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String direction) {

        // Handle custom sorting if provided
        if (sortBy != null && direction != null) {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(sortDirection, sortBy)
            );
        }

        Page<StudentResponseDto> students = studentService.getStudentsByInstitution(institutionId, pageable);
        return ResponseEntity.ok(students);
    }
    @GetMapping("/institution/{institutionId}/search")
    public ResponseEntity<List<StudentResponseDto>> searchStudentsByInstitution(
            @PathVariable Long institutionId,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String guardianName,
            @RequestParam(required = false) String contactNumber) {

        return ResponseEntity.ok(studentService.searchStudentsByInstitution(
                institutionId, studentName, guardianName, contactNumber));
    }

    @GetMapping("/search")
    public ResponseEntity<List<StudentResponseDto>> searchStudents(
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String guardianName,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String contactNumber) {

        return ResponseEntity.ok(studentService.searchStudents(studentName, guardianName, gender, contactNumber));
    }

    @DeleteMapping("/{studentId}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long studentId) {
        studentService.deleteStudent(studentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/unsponsored/urgent/top3")
    public ResponseEntity<List<StudentResponseDto>> getTop3UnsponsoredUrgentStudents() {
        return ResponseEntity.ok(studentService.getTop3UnsponsoredUrgentStudents());
    }

    @GetMapping("/{studentId}/pending-sponsorships")
    public ResponseEntity<List<StudentResponseDto>> getPendingSponsorships(
            @PathVariable Long studentId,
            @RequestParam(required = false, defaultValue = "3") int days) {

        try {
            // ✅ Service method call
            LocalDate fromDate = LocalDate.now().minusDays(days);
            List<StudentResponseDto> result = studentService.getStudentPendingSponsorships(studentId, fromDate);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            // Handle specific exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @GetMapping("/{studentId}/has-pending-sponsorships")
    public ResponseEntity<Boolean> hasPendingSponsorships(
            @PathVariable Long studentId,
            @RequestParam(required = false, defaultValue = "3") int days) {

        try {
            // ✅ Service method call
            LocalDate fromDate = LocalDate.now().minusDays(days);
            boolean hasPending = studentService.hasPendingSponsorships(studentId, fromDate);

            return ResponseEntity.ok(hasPending);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(false);
        }
    }


//    @GetMapping("/unsponsored/urgent")
//    public ResponseEntity<List<StudentResponseDto>> getUnsponsoredUrgentStudents(
//            @RequestParam(defaultValue = "3") int limit) {
//        return ResponseEntity.ok(studentService.getUnsponsoredUrgentStudents(limit));
//    }
//    @GetMapping("/unsponsored")
//    public ResponseEntity<List<StudentResponseDto>> getUnsponsoredStudentsByFinancialRank(
//            @RequestParam(defaultValue = "URGENT") String financialRank,
//            @RequestParam(defaultValue = "3") int limit) {
//        return ResponseEntity.ok(studentService.getUnsponsoredStudentsByFinancialRank(financialRank, limit));
//    }
}
