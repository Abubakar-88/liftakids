package org.liftakids.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.liftakids.dto.payment.PaymentRequestDto;
import org.liftakids.dto.payment.PaymentResponseDto;
import org.liftakids.dto.sponsorship.*;
import org.liftakids.entity.Sponsorship;
import org.liftakids.exception.ResourceNotFoundException;
import org.liftakids.repositories.SponsorshipRepository;
import org.liftakids.service.PaymentService;
import org.liftakids.service.SponsorshipService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sponsorships")
@RequiredArgsConstructor
public class SponsorshipController {
    private final SponsorshipService sponsorshipService;
    private final PaymentService paymentService;
    private final SponsorshipRepository sponsorshipRepository;
    private final ModelMapper modelMapper;

    @PostMapping
    public ResponseEntity<SponsorshipResponseDto> createSponsorship(
            @Valid @RequestBody SponsorshipRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sponsorshipService.createSponsorship(request));
    }
    @GetMapping("/{id}")
    public SponsorshipResponseDto getSponsorshipById(@PathVariable Long id) {
        return sponsorshipService.getSponsorshipById(id);
    }

    @GetMapping("/donor/{donorId}")
    public List<SponsorshipResponseDto> getByDonor(@PathVariable Long donorId) {
        return sponsorshipService.getByDonorId(donorId);
    }
    @GetMapping("/{id}/exists")
    public ResponseEntity<SponsorshipResponseDto> checkSponsorshipExists(@PathVariable Long id) {
        Optional<Sponsorship> sponsorship = sponsorshipRepository.findById(id);
        if (sponsorship.isPresent()) {
            SponsorshipResponseDto response = modelMapper.map(sponsorship.get(), SponsorshipResponseDto.class);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

//    @GetMapping
//    public ResponseEntity<List<SponsorshipResponseDto>> getSponsorshipsByDonorAndStudent(
//            @RequestParam Long donorId,
//            @RequestParam Long studentId) {
//
//        List<Sponsorship> sponsorships = sponsorshipRepository.findByDonorIdAndStudentId(donorId, studentId);
//
//        if (sponsorships.isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }
//
//        List<SponsorshipResponseDto> response = sponsorships.stream()
//                .map(sponsorship -> modelMapper.map(sponsorship, SponsorshipResponseDto.class))
//                .collect(Collectors.toList());
//
//        return ResponseEntity.ok(response);
//    }



    // ========= PAYMENT ENDPOINTS =========

    @PostMapping("/{id}/payment")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponseDto processPayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentRequestDto request) {
        request.setSponsorshipId(id);
        return paymentService.processPayment(request);
    }

//    @GetMapping("/{sponsorshipId}/payments")
//    public List<PaymentResponseDto> getPaymentsBySponsorship(
//            @PathVariable Long sponsorshipId) {
//        return paymentService.getPaymentsBySponsorship(sponsorshipId);
//    }

    @GetMapping("/donor/{donorId}/payments")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByDonor(
            @PathVariable Long donorId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        try {
            List<PaymentResponseDto> payments;

            if (page != null && size != null) {
                // Paginated response
                Page<PaymentResponseDto> paymentPage = paymentService.getPaymentsByDonor(donorId, page, size);
                payments = paymentPage.getContent();

                HttpHeaders headers = new HttpHeaders();
                headers.add("X-Total-Count", String.valueOf(paymentPage.getTotalElements()));
                headers.add("X-Total-Pages", String.valueOf(paymentPage.getTotalPages()));

                return ResponseEntity.ok().headers(headers).body(payments);
            } else {
                // All payments without pagination
                payments = paymentService.getPaymentsByDonor(donorId);
                return ResponseEntity.ok(payments);
            }
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/overdue")
    public ResponseEntity<List<SponsorshipResponseDto>> getOverdueSponsorships() {
        return ResponseEntity.ok(sponsorshipService.getOverdueSponsorships());
    }


    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<SponsorshipResponseDto>> getByStudent(
            @PathVariable Long studentId) {
        return ResponseEntity.ok(sponsorshipService.getByStudentId(studentId));
    }

    @GetMapping("/{sponsorshipId}/payments")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsBySponsorship(
            @PathVariable Long sponsorshipId) {
        try {
            List<PaymentResponseDto> payments = paymentService.getPaymentsBySponsorship(sponsorshipId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/student/{studentId}/payments")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByStudent(
            @PathVariable Long studentId) {
        try {
            List<PaymentResponseDto> payments = paymentService.getPaymentsByStudent(studentId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



//    @GetMapping("/search")
//    public ResponseEntity<Page<SponsorshipResponseDto>> searchSponsorships(
//            @Valid SponsorshipSearchRequest request) {
//        return ResponseEntity.ok(sponsorshipService.searchSponsorships(request));
//    }

    @GetMapping
    public ResponseEntity<Page<SponsorshipResponseDto>> getAllSponsorships(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate,desc") String sort,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String donorName,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String institutionName,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-M-d") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-M-d") LocalDate endDate,
            @RequestParam(required = false) Boolean overdueOnly) {

        // Create pageable with sorting
        Sort.Direction direction = sort.endsWith(",desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        String property = sort.split(",")[0];
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, property));

        // Create search request
        SponsorshipSearchRequest request = new SponsorshipSearchRequest();
        request.setSponsorId(null); // Or pass from params if needed
        request.setStudentName(studentName);
        request.setDonorName(donorName);
        request.setInstitutionName(institutionName);
        request.setStatus(status);
        request.setPaymentMethod(paymentMethod);
        request.setOverdueOnly(overdueOnly);
        request.setStartDate(startDate);
        request.setEndDate(endDate);

        return ResponseEntity.ok(sponsorshipService.searchSponsorships(request, pageable));
    }

}
