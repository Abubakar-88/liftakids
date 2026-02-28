package org.liftakids.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.liftakids.dto.sponsorship.SponsorDetailsDto;
import org.liftakids.dto.sponsorship.SponsorshipRequestDto;
import org.liftakids.dto.sponsorship.SponsorshipResponseDto;
import org.liftakids.dto.sponsorship.SponsorshipSearchRequest;
import org.liftakids.entity.*;
import org.liftakids.exception.BusinessException;
import org.liftakids.exception.ResourceNotFoundException;
import org.liftakids.repositories.DonorRepository;
import org.liftakids.repositories.SponsorshipRepository;
import org.liftakids.repositories.StudentRepository;
import org.liftakids.service.SponsorshipService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SponsorshipServiceImpl implements SponsorshipService {
    private final SponsorshipRepository sponsorshipRepository;
    private final DonorRepository donorRepository;
    private final StudentRepository studentRepository;
    private final ModelMapper modelMapper;
    private final StudentServiceImpl studentService;

    @Override
    @Transactional
    public SponsorshipResponseDto createSponsorship(SponsorshipRequestDto request) {


        // Adjust dates to month boundaries
        LocalDate adjustedStartDate = request.getStartDate().withDayOfMonth(1);
        LocalDate adjustedEndDate = request.getEndDate().withDayOfMonth(request.getEndDate().lengthOfMonth());

        // Check for existing sponsorship with month-adjusted dates
        Optional<Sponsorship> existingSponsorship = sponsorshipRepository
                .findByDonorIdAndStudentIdAndDateRangeOverlap(
                        request.getDonorId(),
                        request.getStudentId(),
                        adjustedStartDate,
                        adjustedEndDate
                );

        if (existingSponsorship.isPresent()) {
            Sponsorship sponsorship = existingSponsorship.get();
            SponsorshipResponseDto response = modelMapper.map(sponsorship, SponsorshipResponseDto.class);
            response.setMessage("Sponsorship already exists for this period. You can make additional payments to this sponsorship.");
            return response;
        }

        Donor donor = donorRepository.findById(request.getDonorId())
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found"));

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        // Create new sponsorship with adjusted dates
        Sponsorship sponsorship = new Sponsorship();
        sponsorship.setDonor(donor);
        sponsorship.setStudent(student);
        sponsorship.setStatus(SponsorshipStatus.PENDING_PAYMENT);
        sponsorship.setPaymentMethod(request.getPaymentMethod());
        sponsorship.setTotalPaidAmount(BigDecimal.ZERO);
        sponsorship.setSponsorStartDate(LocalDate.now());
        sponsorship.setLastPaymentDate(null);
        sponsorship.setPaidUpTo(null);
        sponsorship.setPayments(new ArrayList<>());
        studentService.updateStudentSponsorshipStatus(request.getStudentId());
        // Set monthly amount
        BigDecimal monthlyAmount = request.getMonthlyAmount() != null
                ? request.getMonthlyAmount()
                : student.getRequiredMonthlySupport();

        if (monthlyAmount == null) {
            throw new BusinessException("Monthly amount must be specified either in request or set for student");
        }

        sponsorship.setMonthlyAmount(monthlyAmount);
        sponsorship.setStartDate(adjustedStartDate);
        sponsorship.setEndDate(adjustedEndDate);

        Sponsorship saved = sponsorshipRepository.save(sponsorship);
        SponsorshipResponseDto response = modelMapper.map(saved, SponsorshipResponseDto.class);
        response.setMessage("New sponsorship created successfully");
        return response;
    }
    @Override
    public List<SponsorshipResponseDto> getPendingSponsorshipsForInstitution(Long institutionId) {
        // Find sponsorships that are ACTIVE but have no payments or pending payments
        List<Sponsorship> pendingSponsorships = sponsorshipRepository
                .findByStudentInstitutionIdAndStatus(institutionId, SponsorshipStatus.ACTIVE);

        return pendingSponsorships.stream()
                .map(this::sponsorConvertToDto)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional
    public Page<SponsorshipResponseDto> getAllSponsorships(Pageable pageable) {
        Page<Sponsorship> sponsorships = sponsorshipRepository.findAll(pageable);
        return sponsorships.map(this::sponsorConvertToDto);
    }

    @Override
    public Page<SponsorshipResponseDto> searchSponsorships(SponsorshipSearchRequest request, Pageable pageable) {
        // Convert string enums to actual enum values
        SponsorshipStatus status = request.getStatus() != null ?
                SponsorshipStatus.valueOf(request.getStatus()) : null;

        PaymentMethod paymentMethod = request.getPaymentMethod() != null ?
                PaymentMethod.valueOf(request.getPaymentMethod()) : null;

        // Handle date formatting if needed
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        Page<Sponsorship> sponsorships = sponsorshipRepository.searchSponsorships(
                request.getSponsorId(),
                request.getStudentName(),
                request.getDonorName(),
                request.getInstitutionName(),
                status,
                paymentMethod,
                request.getOverdueOnly(),
                startDate,
                endDate,
                pageable
        );

        return sponsorships.map(this::sponsorConvertToDto);
    }

    // Get sponsorships with PENDING_PAYMENT status
    public List<SponsorshipResponseDto> getPendingPaymentSponsorships(Long institutionId) {
        List<Sponsorship> pendingSponsorships = sponsorshipRepository
                .findByStudentInstitutionIdAndStatus(institutionId, SponsorshipStatus.PENDING_PAYMENT);

        return pendingSponsorships.stream()
                .map(this::sponsorConvertToDto)
                .collect(Collectors.toList());
    }

    // Optimized version
    public List<SponsorshipResponseDto> getPendingPaymentSponsorshipsOptimized(Long institutionId) {
        List<Sponsorship> pendingSponsorships = sponsorshipRepository
                .findPendingPaymentSponsorships(institutionId);

        return pendingSponsorships.stream()
                .map(this::sponsorConvertToDto)
                .collect(Collectors.toList());
    }
    public Map<String, Long> getSponsorshipStatusCounts(Long institutionId) {
        List<Sponsorship> allSponsorships = sponsorshipRepository
                .findByStudentInstitutionId(institutionId);

        return allSponsorships.stream()
                .collect(Collectors.groupingBy(
                        sponsorship -> sponsorship.getStatus().name(),
                        Collectors.counting()
                ));
    }
    // Helper methods
private SponsorshipResponseDto sponsorConvertToDto(Sponsorship sponsorship) {
    Student student = sponsorship.getStudent();
    String paymentStatus = "PENDING";
    String statusMessage = "Sponsorship confirmed but payment pending";
    return SponsorshipResponseDto.builder()
            .id(sponsorship.getId())
            .donorName(sponsorship.getDonor().getName())
            .studentName(student.getStudentName())
            .studentId(student.getStudentId())
            .contactNumber(student.getContactNumber())
            .guardianName(student.getGuardianName())
            .address(student.getAddress())
            .bio(student.getBio())
            .photoUrl(sponsorship.getStudent().getPhotoUrl())
            .financial_rank(sponsorship.getStudent().getFinancial_rank())
            .institutionName(student.getInstitution().getInstitutionName())
            .institutionTeacherName(student.getInstitution().getTeacherName())
            .institutionTeacherDesignation(student.getInstitution().getTeacherDesignation())
            .monthlyAmount(sponsorship.getMonthlyAmount())
            .totalAmount(sponsorship.getTotalAmount())
            .paidUpTo(sponsorship.getPaidUpTo())
            .totalPaidAmount(sponsorship.getTotalPaidAmount())
            .startDate(sponsorship.getStartDate())
            .endDate(sponsorship.getEndDate())
            .nextPaymentDueDate(sponsorship.getNextPaymentDueDate())
            .lastPaymentDate(sponsorship.getLastPaymentDate())
            .totalMonths(sponsorship.getTotalMonths())
            .monthsPaid(sponsorship.getMonthsPaid())
            .totalPayments(sponsorship.getPayments().size())
            .paymentMethod(sponsorship.getPaymentMethod())
            .status(sponsorship.getStatus())
            .paymentStatus(paymentStatus)
            .sponsored(student.isSponsored()) // Make sure this is set properly
            .paymentDue(sponsorship.isPaymentDue())
            .overdue(sponsorship.isOverdue())
            .message(String.format("Sponsor: %s | Amount: ৳%s/month | Status: %s - Awaiting first payment",
                    sponsorship.getDonor().getName(),
                    sponsorship.getMonthlyAmount(),
                    statusMessage))
            .sponsorDetails(mapSponsorDetails(sponsorship.getDonor()))
            .studentStatus(SponsorshipResponseDto.StudentSponsorshipStatusDto.builder()
                    .fullySponsored(student.isFullySponsored())
                    .requiredAmount(student.getRequiredMonthlySupport())
                    .sponsoredAmount(student.getTotalSponsoredAmount())
                    .lastPaymentDate(sponsorship.getLastPaymentDate())
                    .paidUpTo(sponsorship.getPaidUpTo())
                    .paymentStatus("PENDING_FIRST_PAYMENT")
                    .build())
            .build();
}


    private String formatDate(LocalDate date) {
        return date != null ? date.format(DateTimeFormatter.ofPattern("MMM yyyy")) : "";
    }

    @Override
    public List<SponsorshipResponseDto> getByStudentId(Long studentId) {
        return sponsorshipRepository.findByStudentId(studentId)
                .stream()
                .map(this::convertToResDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SponsorshipResponseDto> getOverdueSponsorships() {
        return sponsorshipRepository.findByStatusAndPaidUpToBeforeOrPaidUpToIsNull(
                        SponsorshipStatus.ACTIVE,
                        LocalDate.now().minusMonths(1)
                ).stream()
                .map(this::convertToResDto)
                .collect(Collectors.toList());
    }


private SponsorshipResponseDto convertToResDto(Sponsorship sponsorship) {
    // Initialize DTO with basic mapping
    SponsorshipResponseDto dto = modelMapper.map(sponsorship, SponsorshipResponseDto.class);

    // Force initialization of lazy-loaded relationships
    Student student = sponsorship.getStudent();
    Hibernate.initialize(student.getCurrentSponsorships());

    // Set basic information
    dto.setDonorName(sponsorship.getDonor().getName());
    dto.setStudentName(student.getStudentName());
    dto.setStudentId(student.getStudentId());
    dto.setPhotoUrl(sponsorship.getStudent().getPhotoUrl());
    dto.setFinancial_rank(sponsorship.getStudent().getFinancial_rank());
    dto.setInstitutionName(student.getInstitution().getInstitutionName());
    dto.setTotalPayments(sponsorship.getPayments().size());
    dto.setLastPaymentDate(sponsorship.getLastPaymentDate());

    // Calculate sponsorship status - IMPORTANT FIX
    boolean isSponsored = !student.getCurrentSponsorships().isEmpty();
    dto.setSponsored(isSponsored);
    student.setSponsored(isSponsored); // Sync with entity state

    // Format period information
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
    String period = String.format("%s - %s",
            sponsorship.getStartDate().format(formatter),
            sponsorship.getEndDate().format(formatter));

    // Build comprehensive message
    String statusMessage = String.format(
            "Status: %s | Sponsorships: %d | Sponsored: %s | Period: %s",
            sponsorship.getStatus(),
            student.getCurrentSponsorships().size(),
            isSponsored,
            period
    );
    dto.setMessage(statusMessage);

    // Set sponsor details
    dto.setSponsorDetails(mapSponsorDetails(sponsorship.getDonor()));

    // Calculate student sponsorship status
    BigDecimal requiredAmount = student.getRequiredMonthlySupport() != null ?
            student.getRequiredMonthlySupport() : BigDecimal.ZERO;
    BigDecimal sponsoredAmount = student.getCurrentSponsorships().stream()
            .map(Sponsorship::getTotalPaidAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    dto.setStudentStatus(SponsorshipResponseDto.StudentSponsorshipStatusDto.builder()
            .fullySponsored(sponsoredAmount.compareTo(requiredAmount) >= 0)
            .requiredAmount(requiredAmount)
            .sponsoredAmount(sponsoredAmount)
                    .lastPaymentDate(sponsorship.getLastPaymentDate())
                    .paidUpTo(sponsorship.getPaidUpTo())
            .build());

    return dto;
}
    @Override
    @Transactional
    public SponsorshipResponseDto getSponsorshipById(Long id) {
        Sponsorship sponsorship = sponsorshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sponsorship not found with id: " + id));
        return convertToResDto(sponsorship);
    }
    @Override
    @Transactional
    public List<SponsorshipResponseDto> getByDonorId(Long donorId) {
        return sponsorshipRepository.findByDonorId(donorId).stream()
                .map(this::convertToResDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SponsorshipResponseDto cancelSponsorship(Long id) {
        Sponsorship sponsorship = sponsorshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sponsorship not found"));
        Long studentId = sponsorship.getStudent().getStudentId();
        sponsorship.setStatus(SponsorshipStatus.CANCELLED);
        sponsorshipRepository.save(sponsorship);
        studentService.updateStudentSponsorshipStatus(studentId);
        return convertToDetailedDto(sponsorship);
    }

    private SponsorshipResponseDto convertToDetailedDto(Sponsorship sponsorship) {
        SponsorshipResponseDto dto = modelMapper.map(sponsorship, SponsorshipResponseDto.class);
        dto.setSponsored(sponsorship.getStudent().isSponsored());
        dto.setSponsorDetails(mapSponsorDetails(sponsorship.getDonor()));
        return dto;
    }

    private SponsorDetailsDto mapSponsorDetails(Donor donor) {
        return modelMapper.map(donor, SponsorDetailsDto.class);
    }

    @Override
    public List<SponsorshipResponseDto> getSponsorshipsByDonorId(Long donorId) {
        List<Sponsorship> sponsorships = sponsorshipRepository.findByDonorDonorId(donorId);
        return sponsorships.stream()
                .map(this::sponsorConvertToDto)
                .collect(Collectors.toList());
    }
    @Override
    public Page<SponsorshipResponseDto> getSponsorshipsByDonorId(Long donorId, Pageable pageable) {
        Page<Sponsorship> sponsorships = sponsorshipRepository.findByDonorDonorId(donorId, pageable);
        return sponsorships.map(this::sponsorConvertToDto);
    }

//    @Scheduled(cron = "0 0 2 * * ?") //
//    @Transactional
//    public void expireOldPendingSponsorships() {
//        LocalDate threeDaysAgo = LocalDate.now().minusDays(3);
//
//        List<Sponsorship> expired = sponsorshipRepository
//                .findByStatusAndSponsorStartDateBefore(
//                        SponsorshipStatus.PENDING_PAYMENT,
//                        threeDaysAgo
//                );
//
//        expired.forEach(sponsorship -> {
//            sponsorship.setStatus(SponsorshipStatus.EXPIRED);
//            sponsorship.setUpDateAT(LocalDateTime.now());
//        });
//
//        sponsorshipRepository.saveAll(expired);
//
//        if (!expired.isEmpty()) {
//            log.info("Expired {} pending sponsorships older than {}",
//                    expired.size(), threeDaysAgo);
//        }
//    }



}
