package org.liftakids.controller;

import lombok.RequiredArgsConstructor;
import org.liftakids.dto.payment.ManualPaymentRequestDto;
import org.liftakids.dto.payment.PaymentConfirmationRequestDto;
import org.liftakids.dto.payment.PaymentRequestDto;
import org.liftakids.dto.payment.PaymentResponseDto;
import org.liftakids.dto.sponsorship.SponsorshipResponseDto;
import org.liftakids.entity.Payment;
import org.liftakids.service.FileStorageService;
import org.liftakids.service.PaymentService;
import org.liftakids.service.SponsorshipService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/institution/payments")
@RequiredArgsConstructor
public class InstitutionPaymentController {
    private final SponsorshipService sponsorshipService;
    private final PaymentService paymentService;
   private final FileStorageService fileStorageService;


    // Get PENDING_PAYMENT sponsorships
    @GetMapping("/pending-payment-sponsorships")
    public ResponseEntity<List<SponsorshipResponseDto>> getPendingPaymentSponsorships(
            @RequestParam Long institutionId) {
        try {
            List<SponsorshipResponseDto> pendingSponsorships = sponsorshipService
                    .getPendingPaymentSponsorshipsOptimized(institutionId);
            return ResponseEntity.ok(pendingSponsorships);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }
    @PostMapping("/manual")
    public ResponseEntity<PaymentResponseDto> createManualPayment(
            @RequestBody ManualPaymentRequestDto request) {
        PaymentResponseDto response = paymentService.createManualPayment(request);
        return ResponseEntity.ok(response);
    }
    // Confirm payment
    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponseDto> confirmPayment(
            @RequestBody PaymentConfirmationRequestDto request) {
        PaymentResponseDto response = paymentService.confirmPayment(request);
        return ResponseEntity.ok(response);
    }

    // Get payments by student
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByStudent(
            @PathVariable Long studentId,
            @RequestParam Long institutionId) {
        List<PaymentResponseDto> payments = paymentService.getPaymentsByStudentAndInstitution(studentId, institutionId);
        return ResponseEntity.ok(payments);
    }
    // InstitutionPaymentController.java - নতুন endpoint যোগ করুন
    @GetMapping("/student-payments/{studentId}")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentHistoryByStudent(
            @PathVariable Long studentId) {
        try {
            List<PaymentResponseDto> paymentHistory = paymentService.getPaymentHistoryByStudentId(studentId);
            return ResponseEntity.ok(paymentHistory);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    // Get payments by donor
    @GetMapping("/donor/{donorId}")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByDonor(
            @PathVariable Long donorId,
            @RequestParam Long institutionId) {
        List<PaymentResponseDto> payments = paymentService.getPaymentsByDonorAndInstitution(donorId, institutionId);
        return ResponseEntity.ok(payments);
    }

    @PostMapping("/upload-receipt")
    public ResponseEntity<?> uploadReceipt(@RequestParam("file") MultipartFile file,
                                           @RequestParam(value = "folder", required = false) String folder) {
        try {
            String fileUrl = fileStorageService.uploadFile(file, folder != null ? folder : "receipts");
            return ResponseEntity.ok(Map.of("fileUrl", fileUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "File upload failed: " + e.getMessage()));
        }
    }

    @GetMapping("/completed-payments")
    public ResponseEntity<?> getCompletedPaymentsByInstitution(
            @RequestParam Long institutionId) {
        try {
            List<PaymentResponseDto> completedPayments = paymentService.getCompletedPaymentsByInstitutionId(institutionId);
            return ResponseEntity.ok(completedPayments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Failed to fetch completed payments: " + e.getMessage())
            );
        }
    }

    // Optional: Get all sponsorship status counts
    @GetMapping("/sponsorship-stats")
    public ResponseEntity<Map<String, Object>> getSponsorshipStats(
            @RequestParam Long institutionId) {
        try {
            Map<String, Long> statusCounts = sponsorshipService.getSponsorshipStatusCounts(institutionId);

            Map<String, Object> response = Map.of(
                    "institutionId", institutionId,
                    "statusCounts", statusCounts,
                    "total", statusCounts.values().stream().mapToLong(Long::longValue).sum(),
                    "timestamp", LocalDateTime.now()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Failed to fetch sponsorship stats: " + e.getMessage())
            );
        }
    }
}
