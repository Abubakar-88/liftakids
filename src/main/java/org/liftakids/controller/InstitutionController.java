package org.liftakids.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.liftakids.dto.institute.*;
import org.liftakids.dto.student.StudentResponseDto;
import org.liftakids.exception.ErrorResponse;
import org.liftakids.exception.ResourceNotFoundException;
import org.liftakids.service.InstitutionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/institutions")
@RequiredArgsConstructor
public class InstitutionController {

    private final InstitutionService institutionService;
    private static final Logger log = Logger.getLogger(InstitutionController.class.getName());
    @PostMapping
    public ResponseEntity<InstitutionResponseDto> createInstitution(@Valid @RequestBody InstitutionRequestDto requestDto) {
        InstitutionResponseDto response = institutionService.createInstitution(requestDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequest) {
        LoginResponseDto response = institutionService.login(loginRequest);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
    @GetMapping("/by-union/{unionOrAreaId}")
    public ResponseEntity<?> getInstitutionsByUnion(
            @PathVariable Long unionOrAreaId) {

        List<InstitutionBasicResponse> institutions = institutionService.getByUnionOrArea(unionOrAreaId);

        if (institutions.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No institutions found for union/area ID: " + unionOrAreaId
            );
        }

        return ResponseEntity.ok(institutions);
    }



    @GetMapping
    public ResponseEntity<?> getAllInstitutions(
            @PageableDefault(
                    size = 10,
                    page = 0,
                    sort = "institutionName",
                    direction = Sort.Direction.ASC
            ) Pageable pageable) {

        try {
            Page<InstitutionBasicResponse> result = institutionService.getAllInstitutions(pageable);

            // Handle empty results
            if (result.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("No institutions found"));
            }

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            // Handle invalid pagination/sort parameters
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid pagination parameters: " + e.getMessage()));

        } catch (Exception e) {
            // Handle other unexpected errors
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Failed to retrieve institutions: " + e.getMessage()));
        }
    }

//    @GetMapping
//    public ResponseEntity<List<InstitutionResponseDto>> getAllInstitutions() {
//        return ResponseEntity.ok(institutionService.getAllInstitutions());
//    }

    @GetMapping("/{id}")
    public ResponseEntity<InstitutionResponseDto> getInstitutionById(@PathVariable Long id) {
        return ResponseEntity.ok(institutionService.getInstitutionById(id));
    }
    @PutMapping("/{id}")
    public ResponseEntity<InstitutionResponseDto> updateInstitution(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInstitutionDto requestDto) {
        InstitutionResponseDto updated = institutionService.updateInstitution(id, requestDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInstitution(@PathVariable Long id) {
        institutionService.deleteInstitution(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/approved")
    public ResponseEntity<List<InstitutionResponseDto>> getApprovedInstitutions() {
        return ResponseEntity.ok(institutionService.getApprovedInstitutions());
    }
    
    @PatchMapping("/{institutionId}/status")
    public ResponseEntity<InstitutionResponseDto> updateInstitutionStatus(
            @PathVariable Long institutionId,
            @RequestParam String action, // suspend, activate, reject, approve
            @RequestParam Long adminId,
            @RequestParam(required = false) String reason) {

        InstitutionResponseDto response;

        switch (action.toLowerCase()) {
            case "suspend":
                response = institutionService.suspendInstitution(institutionId, adminId, reason);
                break;
            case "activate":
                response = institutionService.activateInstitution(institutionId, adminId);
                break;
            case "reject":
                response = institutionService.rejectInstitution(institutionId, adminId, reason);
                break;
            case "approve":
                response = institutionService.approveInstitution(institutionId, adminId, reason);
                break;
            default:
                throw new IllegalArgumentException("Invalid action: " + action +
                        ". Valid actions: suspend, activate, reject, approve");
        }

        return ResponseEntity.ok(response);
    }

    // POST endpoint to approve a specific institution
//    @PostMapping("/{institutionId}/approve")
//    public ResponseEntity<InstitutionResponseDto> approveInstitution(
//            @PathVariable Long institutionId,
//            @RequestParam Long adminId,
//            @RequestParam(required = false) String approvalNotes) {
//
//        return ResponseEntity.ok(institutionService.approveInstitution(institutionId, adminId, approvalNotes));
//    }
    @GetMapping("/type/{type}")
    public ResponseEntity<List<InstitutionResponseDto>> getByType(@PathVariable String type) {
        return ResponseEntity.ok(institutionService.getInstitutionsByType(type));
    }

    @GetMapping("/search")
    public ResponseEntity<InstitutionResponseDto> getInstitutionByIdOrName(@RequestParam String value) {
        InstitutionResponseDto response = institutionService.getByIdOrName(value);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/name")
    public ResponseEntity<String> getInstitutionNameById(@PathVariable Long id) {
        String name = institutionService.getInstitutionNameById(id);
        return ResponseEntity.ok(name);
    }
    @GetMapping("/all")
    public ResponseEntity<List<InstitutionResponseDto>> getAllInstitutions() {
        List<InstitutionResponseDto> institutions = institutionService.getAllInstitutionsList();
        return ResponseEntity.ok(institutions);
    }
    @GetMapping("/{institutionId}/students-with-sponsors")
    public ResponseEntity<Page<StudentResponseDto>> getAllStudentsWithSponsorsByInstitution(
            @PathVariable Long institutionId,
            @PageableDefault(
                    size = 20,
                    page = 0,
                    sort = "studentName",
                    direction = Sort.Direction.ASC
            ) Pageable pageable) {

        try {
            Page<StudentResponseDto> students = institutionService.getAllStudentsWithSponsorsByInstitution(institutionId, pageable);

            if (students.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Page.empty());
            }

            return ResponseEntity.ok(students);

        } catch (ResourceNotFoundException e) {
            log.severe("Institution not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Page.empty());

        } catch (Exception e) {
            // Log full stack trace
            log.severe("Unexpected error while fetching students with sponsors: " + e.toString());
            e.printStackTrace();  // prints full stack trace in console
            return ResponseEntity.internalServerError().body(Page.empty());
        }
    }

    @GetMapping("/statistics/status")
    public ResponseEntity<StatusStatisticsDto> getStatusStatistics() {
        log.info("API: Fetching institution status statistics");
        StatusStatisticsDto statistics = institutionService.getStatusStatistics();
        return ResponseEntity.ok(statistics);
    }

    // Optional: Get statistics with breakdown
    @GetMapping("/statistics/detailed")
    public ResponseEntity<Map<String, Object>> getDetailedStatistics() {
        log.info("API: Fetching detailed institution statistics");

        Map<String, Object> response = new HashMap<>();

        // Get basic statistics
        StatusStatisticsDto basicStats = institutionService.getStatusStatistics();
        response.put("basic", basicStats);

        // Add timestamp
        response.put("timestamp", LocalDateTime.now());

        // Add percentages
        if (basicStats.getTotal() > 0) {
            Map<String, Double> percentages = new HashMap<>();
            percentages.put("approved", (basicStats.getApproved() * 100.0) / basicStats.getTotal());
            percentages.put("pending", (basicStats.getPending() * 100.0) / basicStats.getTotal());
            percentages.put("rejected", (basicStats.getRejected() * 100.0) / basicStats.getTotal());
            percentages.put("suspended", (basicStats.getSuspended() * 100.0) / basicStats.getTotal());
            response.put("percentages", percentages);
        }

        return ResponseEntity.ok(response);
    }


}
