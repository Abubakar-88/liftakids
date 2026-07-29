package org.liftakids.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.liftakids.dto.sponsorship.SponsorDetailsDto;
import org.liftakids.dto.sponsorship.SponsorshipRequestDto;
import org.liftakids.dto.sponsorship.SponsorshipResponseDto;
import org.liftakids.dto.sponsorship.SponsorshipSearchRequest;
import org.liftakids.entity.*;
import org.liftakids.entity.enm.NotificationStatus;
import org.liftakids.entity.enm.NotificationType;
import org.liftakids.entity.enm.UserType;
import org.liftakids.exception.BusinessException;
import org.liftakids.exception.ResourceNotFoundException;
import org.liftakids.exception.UnauthorizedException;
import org.liftakids.repositories.DonorRepository;
import org.liftakids.repositories.NotificationRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class SponsorshipServiceImpl implements SponsorshipService {
    private final SponsorshipRepository sponsorshipRepository;
    private final DonorRepository donorRepository;
    private final StudentRepository studentRepository;
    private final ModelMapper modelMapper;
    private final StudentServiceImpl studentService;
    private final NotificationRepository notificationRepository;
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
        sendPendingPaymentNotificationToInstitution(saved);
        sendPendingPaymentNotificationToDonor(saved);
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
    @Transactional
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
    @Override
    @Transactional
    public List<SponsorshipResponseDto> getPendingPaymentSponsorshipsOptimized(Long institutionId) {
        List<Sponsorship> pendingSponsorships = sponsorshipRepository
                .findPendingPaymentSponsorships(institutionId);

        return pendingSponsorships.stream()
                .map(this::sponsorConvertToDto)
                .collect(Collectors.toList());
    }

@Override
@Transactional
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
    @Transactional
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
    @Transactional
    public List<SponsorshipResponseDto> getSponsorshipsByDonorId(Long donorId) {
        List<Sponsorship> sponsorships = sponsorshipRepository.findByDonorDonorId(donorId);
        return sponsorships.stream()
                .map(this::sponsorConvertToDto)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional
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


    // Sponsorship Cancel or remove
// ========== CANCEL SPONSORSHIP (Donor) ==========
    @Override
    @Transactional
    public SponsorshipResponseDto cancelSponsorship(Long sponsorshipId, Long donorId, String reason) {
        log.info("Cancelling sponsorship: {} by donor: {}", sponsorshipId, donorId);

        Sponsorship sponsorship = sponsorshipRepository.findById(sponsorshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Sponsorship not found with id: " + sponsorshipId));

        // Verify donor owns this sponsorship
        if (!sponsorship.getDonor().getDonorId().equals(donorId)) {
            throw new UnauthorizedException("You are not authorized to cancel this sponsorship");
        }

        // 🔥 Updated: Allow cancellation for PENDING_PAYMENT, ACTIVE, and EXPIRED
        if (sponsorship.getStatus() != SponsorshipStatus.PENDING_PAYMENT &&
                sponsorship.getStatus() != SponsorshipStatus.ACTIVE &&
                sponsorship.getStatus() != SponsorshipStatus.EXPIRED) {
            throw new IllegalStateException("This sponsorship cannot be cancelled. Current status: " + sponsorship.getStatus());
        }

        String cancelReason = (reason == null || reason.trim().isEmpty()) ? "Cancelled by donor" : reason;

        sponsorship.cancel(donorId, cancelReason);
        Sponsorship saved = sponsorshipRepository.save(sponsorship);

        // Update student
        Student student = sponsorship.getStudent();
        student.setSponsored(false);
        student.removeSponsorship(sponsorship);
        studentRepository.save(student);

        // Send cancellation notifications
        sendCancellationNotifications(saved, cancelReason);

        return convertToDto(saved);
    }
    // ========== REMOVE SPONSORSHIP (Admin) ==========
    @Override
    @Transactional
    public SponsorshipResponseDto removeSponsorship(Long sponsorshipId, Long adminId) {


        Sponsorship sponsorship = sponsorshipRepository.findById(sponsorshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Sponsorship not found with id: " + sponsorshipId));

        sponsorship.remove(adminId);
        Sponsorship saved = sponsorshipRepository.save(sponsorship);

        // Update student
        Student student = sponsorship.getStudent();
        student.setSponsored(false);
        student.removeSponsorship(sponsorship);
        studentRepository.save(student);

        // Send removal notification
        sendRemovalNotification(saved, adminId);

        return convertToDto(saved);
    }


    private void sendCancellationNotifications(Sponsorship sponsorship, String reason) {
        try {
            String formattedStartDate = formatDate(sponsorship.getStartDate());
            String formattedEndDate = formatDate(sponsorship.getEndDate());

            // Notification to donor
            String donorMessage = String.format(
                    "Your sponsorship for %s has been cancelled.\n\n" +
                            "📌 Student: %s\n" +
                            "💰 Monthly Amount: ৳%s\n" +
                            "📅 Original Period: %s to %s\n" +
                            "📝 Reason: %s",
                    sponsorship.getStudent().getStudentName(),
                    sponsorship.getStudent().getStudentName(),
                    sponsorship.getMonthlyAmount(),
                    formattedStartDate,
                    formattedEndDate,
                    reason
            );

            Notification donorNotification = Notification.builder()
                    .userType(UserType.DONOR)
                    .userId(sponsorship.getDonor().getDonorId())
                    .donor(sponsorship.getDonor())
                    .title("❌ Sponsorship Cancelled")
                    .message(donorMessage)
                    .shortMessage("Your sponsorship has been cancelled")
                    .type(NotificationType.SPONSORSHIP_CANCELLED)
                    .status(NotificationStatus.UNREAD)
                    .relatedEntityType("SPONSORSHIP")
                    .relatedEntityId(sponsorship.getId())
                    .relatedEntityName(sponsorship.getStudent().getStudentName())
                    .priority("HIGH")
                    .category("FINANCIAL")
                    .senderName("System")
                    .senderType("SYSTEM")
                    // 🔥 Required non-null fields with defaults
                    .emailSent(false)
                    .smsSent(false)
                    .pushSent(false)
                    .inAppSent(true)
                    .version(0L)
                    .build();

            notificationRepository.save(donorNotification);

            // Notification to institution
            Institutions institution = sponsorship.getStudent().getInstitution();
            String institutionMessage = String.format(
                    "Sponsorship for student %s has been cancelled by donor %s.\n\n" +
                            "📌 Student: %s\n" +
                            "💰 Monthly Amount: ৳%s\n" +
                            "📅 Original Period: %s to %s\n" +
                            "📝 Reason: %s",
                    sponsorship.getStudent().getStudentName(),
                    sponsorship.getDonor().getName(),
                    sponsorship.getStudent().getStudentName(),
                    sponsorship.getMonthlyAmount(),
                    formattedStartDate,
                    formattedEndDate,
                    reason
            );

            Notification institutionNotification = Notification.builder()
                    .userType(UserType.INSTITUTION)
                    .userId(institution.getInstitutionsId())
                    .institution(institution)
                    .title("❌ Sponsorship Cancelled by Donor")
                    .message(institutionMessage)
                    .shortMessage("Sponsorship has been cancelled by donor")
                    .type(NotificationType.SPONSORSHIP_CANCELLED)
                    .status(NotificationStatus.UNREAD)
                    .actionUrl("/institution/sponsorships")
                    .actionText("View Details")
                    .icon("error")
                    .relatedEntityType("SPONSORSHIP")
                    .relatedEntityId(sponsorship.getId())
                    .relatedEntityName(sponsorship.getStudent().getStudentName())
                    .priority("HIGH")
                    .category("FINANCIAL")
                    .senderName("System")
                    .senderType("SYSTEM")
                    // 🔥 Required non-null fields with defaults
                    .emailSent(false)
                    .smsSent(false)
                    .pushSent(false)
                    .inAppSent(true)
                    .version(0L)
                    .build();

            notificationRepository.save(institutionNotification);

            log.info("✅ Cancellation notifications sent for sponsorship: {}", sponsorship.getId());
        } catch (Exception e) {
            log.error("Failed to send cancellation notifications: {}", e.getMessage());
        }
    }

    private void sendRemovalNotification(Sponsorship sponsorship, Long adminId) {
        try {
            String formattedStartDate = formatDate(sponsorship.getStartDate());
            String formattedEndDate = formatDate(sponsorship.getEndDate());

            String message = String.format(
                    "Your sponsorship for %s has been removed by the administrator.\n\n" +
                            "📌 Student: %s\n" +
                            "💰 Monthly Amount: ৳%s\n" +
                            "📅 Original Period: %s to %s\n\n" +
                            "Please contact support for more information.",
                    sponsorship.getStudent().getStudentName(),
                    sponsorship.getStudent().getStudentName(),
                    sponsorship.getMonthlyAmount(),
                    formattedStartDate,
                    formattedEndDate
            );

            Notification donorNotif = Notification.builder()
                    .userType(UserType.DONOR)
                    .userId(sponsorship.getDonor().getDonorId())
                    .donor(sponsorship.getDonor())
                    .title("⚠️ Sponsorship Removed by Admin")
                    .message(message)
                    .shortMessage("Your sponsorship has been removed by administrator")
                    .type(NotificationType.SPONSORSHIP_REMOVED)
                    .status(NotificationStatus.UNREAD)
                    .actionUrl("/donor/sponsored-students")
                    .actionText("View Details")
                    .icon("error")
                    .relatedEntityType("SPONSORSHIP")
                    .relatedEntityId(sponsorship.getId())
                    .relatedEntityName(sponsorship.getStudent().getStudentName())
                    .priority("HIGH")
                    .category("SECURITY")
                    .senderName("System Admin")
                    .senderType("ADMIN")
                    .senderId(adminId)
                    // 🔥 Required non-null fields with defaults
                    .emailSent(false)
                    .smsSent(false)
                    .pushSent(false)
                    .inAppSent(true)
                    .version(0L)
                    .build();

            notificationRepository.save(donorNotif);
            log.info("✅ Removal notification sent to donor: {} for sponsorship: {}",
                    sponsorship.getDonor().getName(), sponsorship.getId());
        } catch (Exception e) {
            log.error("Failed to send removal notification for sponsorship {}: {}",
                    sponsorship.getId(), e.getMessage());
        }
    }
    @Override
    public boolean canBeCancelled(Long sponsorshipId, Long donorId) {
        log.info("Checking if sponsorship {} can be cancelled by donor {}", sponsorshipId, donorId);

        Sponsorship sponsorship = sponsorshipRepository.findById(sponsorshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Sponsorship not found"));

        // Check if donor owns this sponsorship
        if (!sponsorship.getDonor().getDonorId().equals(donorId)) {
            log.warn("Donor {} does not own sponsorship {}", donorId, sponsorshipId);
            return false;
        }

        // ✅ Allow cancellation for PENDING_PAYMENT, ACTIVE, and EXPIRED
        boolean canCancel = (sponsorship.getStatus() == SponsorshipStatus.PENDING_PAYMENT ||
                sponsorship.getStatus() == SponsorshipStatus.ACTIVE ||
                sponsorship.getStatus() == SponsorshipStatus.EXPIRED);

        log.info("Sponsorship status: {}, Can cancel: {}", sponsorship.getStatus(), canCancel);

        return canCancel;
    }
    // ========== CONVERT TO DTO ==========
    private SponsorshipResponseDto convertToDto(Sponsorship sponsorship) {
        Student student = sponsorship.getStudent();

        // Calculate payment status without external method
        String paymentStatus = "UNKNOWN";
        if (sponsorship.getStatus() == SponsorshipStatus.PENDING_PAYMENT) {
            paymentStatus = "AWAITING_INSTITUTION_CONFIRMATION";
        } else if (sponsorship.getStatus() == SponsorshipStatus.ACTIVE) {
            if (sponsorship.isOverdue()) {
                paymentStatus = "OVERDUE";
            } else if (sponsorship.isPaymentDue()) {
                paymentStatus = "DUE";
            } else {
                paymentStatus = "UP_TO_DATE";
            }
        } else if (sponsorship.getStatus() == SponsorshipStatus.CANCELLED) {
            paymentStatus = "CANCELLED";
        } else if (sponsorship.getStatus() == SponsorshipStatus.ACTIVE) {
            paymentStatus = "COMPLETED";
        }

        // Format period display
        String periodDisplay = String.format("%s - %s",
                formatDate(sponsorship.getStartDate()),
                formatDate(sponsorship.getEndDate()));

        // Get payment method display - FIXED
        String paymentMethodDisplay = "N/A";
        if (sponsorship.getPaymentMethod() != null) {
            paymentMethodDisplay = sponsorship.getPaymentMethod().name();
            // Or if you have getDisplayName method:
            // paymentMethodDisplay = sponsorship.getPaymentMethod().getDisplayName();
        }

        return SponsorshipResponseDto.builder()
                .id(sponsorship.getId())
                .donorName(sponsorship.getDonor().getName())
                .studentName(student.getStudentName())
                .studentId(student.getStudentId())
                .address(student.getAddress())
                .contactNumber(student.getContactNumber())
                .guardianName(student.getGuardianName())
                .photoUrl(student.getPhotoUrl())
                .bio(student.getBio())
                .institutionName(student.getInstitution().getInstitutionName())
                .institutionTeacherName(student.getInstitution().getTeacherName())
                .institutionTeacherDesignation(student.getInstitution().getTeacherDesignation())
                .financial_rank(student.getFinancial_rank())
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
                .totalPayments(sponsorship.getPayments() != null ? sponsorship.getPayments().size() : 0)
                .paymentMethod(sponsorship.getPaymentMethod())
                .status(sponsorship.getStatus())
                .paymentStatus(paymentStatus)
                .periodDisplay(periodDisplay)
                .paymentMethodDisplay(paymentMethodDisplay)
                .paymentDue(sponsorship.isPaymentDue())
                .overdue(sponsorship.isOverdue())
                .sponsored(student.isSponsored())
                .build();
    }

    private void sendPendingPaymentNotificationToInstitution(Sponsorship sponsorship) {
        try {
            Institutions institution = sponsorship.getStudent().getInstitution();
            String formattedStartDate = formatDate(sponsorship.getStartDate());
            String formattedEndDate = formatDate(sponsorship.getEndDate());

            String message = String.format(
                    "A new sponsorship has been created for student %s by donor %s.\n\n" +
                            "📌 Student: %s\n" +
                            "💰 Monthly Amount: ৳%s\n" +
                            "📅 Period: %s to %s\n" +
                            "💵 Total Amount: ৳%s\n\n" +
                            "Please confirm payment to activate the sponsorship.",
                    sponsorship.getStudent().getStudentName(),
                    sponsorship.getDonor().getName(),
                    sponsorship.getStudent().getStudentName(),
                    sponsorship.getMonthlyAmount(),
                    formattedStartDate,
                    formattedEndDate,
                    sponsorship.getTotalAmount()
            );

            Notification instNotif = Notification.builder()
                    .userType(UserType.INSTITUTION)
                    .userId(institution.getInstitutionsId())
                    .institution(institution)
                    .title("⚠️ New Sponsorship - Payment Pending")
                    .message(message)
                    .shortMessage("New sponsorship pending payment confirmation")
                    .type(NotificationType.PENDING_PAYMENT)
                    .status(NotificationStatus.UNREAD)
                    .actionUrl("/institution/pending-payments")
                    .actionText("View Details")
                    .icon("warning")
                    .relatedEntityType("SPONSORSHIP")
                    .relatedEntityId(sponsorship.getId())
                    .relatedEntityName(sponsorship.getStudent().getStudentName())
                    .priority("HIGH")
                    .category("FINANCIAL")
                    .senderName("System")
                    .senderType("SYSTEM")
                    // Required non-null fields
                    .emailSent(false)
                    .smsSent(false)
                    .pushSent(false)
                    .inAppSent(true)
                    .version(0L)
                    .build();

            notificationRepository.save(instNotif);
            log.info("✅ Pending payment notification sent to institution: {}", institution.getInstitutionName());
        } catch (Exception e) {
            log.error("Failed to send notification to institution: {}", e.getMessage());
        }
    }
    private void sendPendingPaymentNotificationToDonor(Sponsorship sponsorship) {
        try {
            String formattedStartDate = formatDate(sponsorship.getStartDate());
            String formattedEndDate = formatDate(sponsorship.getEndDate());

            String message = String.format(
                    "Your sponsorship for %s has been created.\n\n" +
                            "📌 Student: %s\n" +
                            "💰 Monthly Amount: ৳%s\n" +
                            "📅 Period: %s to %s\n" +
                            "💵 Total Amount: ৳%s\n\n" +
                            "The institution will confirm your payment shortly. Once confirmed, your sponsorship will become ACTIVE.",
                    sponsorship.getStudent().getStudentName(),
                    sponsorship.getStudent().getStudentName(),
                    sponsorship.getMonthlyAmount(),
                    formattedStartDate,
                    formattedEndDate,
                    sponsorship.getTotalAmount()
            );

            Notification donorNotif = Notification.builder()
                    .userType(UserType.DONOR)
                    .userId(sponsorship.getDonor().getDonorId())
                    .donor(sponsorship.getDonor())
                    .title("Sponsorship Created - Awaiting Payment Confirmation")
                    .message(message)
                    .shortMessage("Your sponsorship is pending institution confirmation")
                    .type(NotificationType.SPONSORSHIP_CREATED)
                    .status(NotificationStatus.UNREAD)
                    .actionUrl("/donor/sponsored-students")
                    .actionText("View Details")
                    .icon("info")
                    .relatedEntityType("SPONSORSHIP")
                    .relatedEntityId(sponsorship.getId())
                    .relatedEntityName(sponsorship.getStudent().getStudentName())
                    .priority("MEDIUM")
                    .category("FINANCIAL")
                    .senderName("System")
                    .senderType("SYSTEM")
                    // Required non-null fields
                    .emailSent(false)
                    .smsSent(false)
                    .pushSent(false)
                    .inAppSent(true)
                    .version(0L)
                    .build();

            notificationRepository.save(donorNotif);
            log.info("✅ Pending payment notification sent to donor: {}", sponsorship.getDonor().getName());
        } catch (Exception e) {
            log.error("Failed to send notification to donor: {}", e.getMessage());
        }
    }
}
