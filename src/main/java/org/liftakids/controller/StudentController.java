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
import org.liftakids.repositories.StudentRepository;
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
    private final StudentRepository studentRepository;
    private final StudentService studentService;
    private final SponsorshipRepository sponsorshipRepository;

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

    //    @GetMapping("/search")
//    public ResponseEntity<List<StudentResponseDto>> searchStudents(
//            @RequestParam(required = false) String studentName,
//            @RequestParam(required = false) String guardianName,
//            @RequestParam(required = false) String gender,
//            @RequestParam(required = false) String contactNumber) {
//
//        return ResponseEntity.ok(studentService.searchStudents(studentName, guardianName, gender, contactNumber));
//    }
    @GetMapping("/search")
    public ResponseEntity<Page<StudentResponseDto>> searchStudents(
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String guardianName,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String contactNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "studentName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        // sorting configure
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        // pageble object
        Pageable pageable = PageRequest.of(page, size, sort);

        // paginate by search
        Page<StudentResponseDto> resultPage = studentService.searchStudents(
                studentName,
                guardianName,
                gender,
                contactNumber,
                pageable
        );

        return ResponseEntity.ok(resultPage);
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
    @GetMapping("/unsponsored/urgent/top")
    public ResponseEntity<List<StudentResponseDto>> getTopUnsponsoredUrgentStudents(
            @RequestParam(value = "limit", defaultValue = "4") int limit) {
        return ResponseEntity.ok(studentService.getTopUnsponsoredUrgentStudents(limit));
    }

    // 1. Get students by DISTRICT
    @GetMapping("/by-district/{districtId}")
    public ResponseEntity<Page<StudentResponseDto>> getStudentsByDistrict(
            @PathVariable Long districtId,
            @PageableDefault(
                    size = 10,
                    page = 0,
                    sort = "studentName",
                    direction = Sort.Direction.ASC
            ) Pageable pageable) {
        return ResponseEntity.ok(studentService.getStudentsByDistrict(districtId, pageable));
    }

    // 2. Get students by THANA
    @GetMapping("/by-thana/{thanaId}")
    public ResponseEntity<Page<StudentResponseDto>> getStudentsByThana(
            @PathVariable Long thanaId,
            @PageableDefault(
                    size = 10,
                    page = 0,
                    sort = "studentName",
                    direction = Sort.Direction.ASC
            ) Pageable pageable) {
        return ResponseEntity.ok(studentService.getStudentsByThana(thanaId, pageable));
    }

    // 3. Get students by UNION
    @GetMapping("/by-union/{unionId}")
    public ResponseEntity<Page<StudentResponseDto>> getStudentsByUnion(
            @PathVariable Long unionId,
            @PageableDefault(
                    size = 10,
                    page = 0,
                    sort = "studentName",
                    direction = Sort.Direction.ASC
            ) Pageable pageable) {
        return ResponseEntity.ok(studentService.getStudentsByUnion(unionId, pageable));
    }

    // 4. Get students by DIVISION
    @GetMapping("/by-division/{divisionId}")
    public ResponseEntity<Page<StudentResponseDto>> getStudentsByDivision(
            @PathVariable Long divisionId,
            @PageableDefault(
                    size = 10,
                    page = 0,
                    sort = "studentName",
                    direction = Sort.Direction.ASC
            ) Pageable pageable) {
        return ResponseEntity.ok(studentService.getStudentsByDivision(divisionId, pageable));
    }

    @GetMapping("/debug")
    public ResponseEntity<String> debug() {

        long count = studentRepository.count();
        List<Student> students = studentRepository.findAll();

        return ResponseEntity.ok(
                "COUNT = " + count + "\n" +
                        "LIST SIZE = " + students.size()
        );
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
