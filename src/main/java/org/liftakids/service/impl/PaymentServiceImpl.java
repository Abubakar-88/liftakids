package org.liftakids.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.liftakids.dto.payment.ManualPaymentRequestDto;
import org.liftakids.dto.payment.PaymentConfirmationRequestDto;
import org.liftakids.dto.payment.PaymentRequestDto;
import org.liftakids.dto.payment.PaymentResponseDto;
import org.liftakids.dto.sponsorship.SponsorshipResponseDto;
import org.liftakids.entity.*;
import org.liftakids.exception.BusinessException;
import org.liftakids.exception.ResourceNotFoundException;
import org.liftakids.repositories.DonorRepository;
import org.liftakids.repositories.PaymentRepository;
import org.liftakids.repositories.SponsorshipRepository;
import org.liftakids.repositories.StudentRepository;
import org.liftakids.service.PaymentService;
import org.liftakids.service.Util.EmailService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final SponsorshipRepository sponsorshipRepository;
    private final ModelMapper modelMapper;
    private final StudentServiceImpl studentService;
    private final StudentRepository studentRepository;
    private final DonorRepository donorRepository;
    private final EmailService emailService;
    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);
    @Override
    @Transactional
    public PaymentResponseDto processPayment(PaymentRequestDto request) {
        // Validate request
        if (request.getSponsorshipId() == null || request.getEndDate() == null || request.getAmount() == null) {
            throw new BusinessException("Required payment fields cannot be null");
        }

        // Fetch sponsorship
        Sponsorship sponsorship = sponsorshipRepository.findById(request.getSponsorshipId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sponsorship not found with id: " + request.getSponsorshipId()));

        // Process payment based on type
        Payment payment = request.isTestPayment()
                ? processTestPayment(sponsorship, request)
                : processRegularPayment(sponsorship, request);

        // Save and return
        paymentRepository.save(payment);
        sponsorshipRepository.save(sponsorship);
        return convertToDto(payment);
    }

    private Payment processRegularPayment(Sponsorship sponsorship, PaymentRequestDto request) {
        // Validate input
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("Start date and end date must be provided");
        }

        // Adjust dates to first day of month for start and last day for end
        LocalDate adjustedStartDate = request.getStartDate().withDayOfMonth(1);
        LocalDate adjustedEndDate = request.getEndDate().withDayOfMonth(request.getEndDate().lengthOfMonth());

        // Validate payment
        validatePayment(sponsorship, adjustedStartDate, adjustedEndDate, request.getAmount());

        // Build and save payment
        Payment payment = Payment.builder(
                        sponsorship,
                        LocalDate.now(),
                        request.getAmount(),
                        request.getPaymentMethod(),
                        PaymentStatus.COMPLETED,
                        adjustedStartDate,
                        adjustedEndDate
                )
                .cardLastFour(extractCardLastFour(request))
                .transactionId(generateTransactionId())
                .build();

        // Update sponsorship
        updateSponsorship(sponsorship, payment, request.getAmount());

        return payment;
    }
    private String generateTransactionId() {
        // Get current timestamp in milliseconds
        long timestamp = System.currentTimeMillis();

        // Generate random 4-digit number between 1000-9999
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);

        return String.format("TXN-%d-%04d", timestamp, random);
    }
    // Helper method for card details
    private String extractCardLastFour(PaymentRequestDto request) {
        if (request.getPaymentMethod() == PaymentMethod.CREDIT_CARD &&
                request.getCardDetails() != null) {
            String cardNumber = request.getCardDetails().getCardNumber();
            return cardNumber.substring(Math.max(0, cardNumber.length() - 4));
        }
        return null;
    }

    // Helper method for sponsorship updates
    private void updateSponsorship(Sponsorship sponsorship, Payment payment, BigDecimal amount) {
        // Update payment list and amounts
        sponsorship.getPayments().add(payment);
        sponsorship.setTotalPaidAmount(sponsorship.getTotalPaidAmount().add(amount));
        sponsorship.setTotalAmount(sponsorship.getTotalAmount().add(amount));
        sponsorship.setTotalMonths(payment.getTotalMonths());
        sponsorship.setLastPaymentDate(LocalDate.now());
        sponsorship.setStartDate(payment.getStartDate());
        sponsorship.setEndDate(payment.getEndDate());
        sponsorship.setNextPaymentDueDate(payment.getPaidUpTo());

        // Extend paidUpTo date if new payment extends further
        if (sponsorship.getPaidUpTo() == null ||
                payment.getEndDate().isAfter(sponsorship.getPaidUpTo())) {
            sponsorship.setPaidUpTo(payment.getEndDate());
        }

        // Update status
        sponsorship.setStatus(
                payment.getEndDate().isAfter(sponsorship.getEndDate().minusDays(1))
                        ? SponsorshipStatus.COMPLETED
                        : SponsorshipStatus.ACTIVE
        );

        // Force update of student sponsorship status
        Student student = sponsorship.getStudent();
        student.updateSponsorshipStatus();
        studentService.updateStudentSponsorshipStatus(student.getStudentId());
    }
    private void validatePayment(Sponsorship sponsorship, LocalDate startDate, LocalDate endDate, BigDecimal amount) {
        // First adjust the dates to month boundaries
        LocalDate adjustedStartDate = startDate.withDayOfMonth(1);
        LocalDate adjustedEndDate = endDate.withDayOfMonth(endDate.lengthOfMonth());

        // Validate dates are within sponsorship period (using adjusted dates)
        if (adjustedStartDate.isBefore(sponsorship.getSponsorStartDate())) {
            throw new BusinessException("Cannot pay for dates before sponsorship start");
        }

        // Payment must be positive
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Payment amount must be positive");
        }

        // Check for overlapping payments (using adjusted dates)
        List<Payment> existingPayments = paymentRepository.findBySponsorshipId(sponsorship.getId());
        for (Payment existingPayment : existingPayments) {
            LocalDate existingStart = existingPayment.getStartDate().withDayOfMonth(1);
            LocalDate existingEnd = existingPayment.getEndDate().withDayOfMonth(existingPayment.getEndDate().lengthOfMonth());

            if (datesOverlap(existingStart, existingEnd, adjustedStartDate, adjustedEndDate)) {
                throw new BusinessException(String.format(
                        "Payment already exists for overlapping period: %s to %s",
                        existingPayment.getStartDate().format(DateTimeFormatter.ofPattern("MMM yyyy")),
                        existingPayment.getEndDate().format(DateTimeFormatter.ofPattern("MMM yyyy"))
                ));
            }
        }

        // Calculate expected amount with full months
        long months = ChronoUnit.MONTHS.between(adjustedStartDate, adjustedEndDate) + 1;
        BigDecimal expectedAmount = sponsorship.getMonthlyAmount().multiply(BigDecimal.valueOf(months));

        if (amount.compareTo(expectedAmount) != 0) {
            throw new BusinessException(String.format(
                    "Payment amount must be %.2f for %d months",
                    expectedAmount, months));
        }
    }
    // Helper method to check date overlaps
    private boolean datesOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        return !start1.isAfter(end2) && !start2.isAfter(end1);
    }
    private Payment processTestPayment(Sponsorship sponsorship, PaymentRequestDto request) {
        // Adjust dates
        LocalDate adjustedStartDate = request.getStartDate().withDayOfMonth(1);
        LocalDate adjustedEndDate = request.getEndDate().withDayOfMonth(request.getEndDate().lengthOfMonth());

        // Basic validation with adjusted dates
        if (adjustedEndDate.isBefore(sponsorship.getStartDate())) {
            throw new BusinessException("Cannot create test payment before sponsorship start");
        }
        validatePayment(sponsorship, adjustedStartDate, adjustedEndDate, request.getAmount());
      //  int totalMonths = (int) ChronoUnit.MONTHS.between(adjustedStartDate, adjustedEndDate) + 1;

        // Build and save payment
        Payment payment = Payment.builder(
                        sponsorship,
                        LocalDate.now(),
                        request.getAmount(),
                        request.getPaymentMethod(),
                        PaymentStatus.COMPLETED,
                        adjustedStartDate,
                        adjustedEndDate
                )
                .cardLastFour(extractCardLastFour(request))
                .transactionId(generateTransactionId())
                .build();

        sponsorship.getPayments().add(payment);
        sponsorship.setPaidUpTo(adjustedEndDate);

        return payment;
    }
    public List<PaymentResponseDto> getPaymentsByDonor(Long donorId) {
        List<Payment> payments = paymentRepository.findByDonorId(donorId);

        if (payments.isEmpty()) {
            throw new ResourceNotFoundException("No payments found for donor with id: " + donorId);
        }

        return payments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<PaymentResponseDto> getPaymentsByDonor(Long donorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Payment> payments = paymentRepository.findByDonorId(donorId, pageable);

        if (payments.isEmpty()) {
            throw new ResourceNotFoundException("No payments found for donor with id: " + donorId);
        }

        return payments.map(this::convertToDto);
    }


    @Override
    @Transactional
    public List<PaymentResponseDto> getPaymentsBySponsorship(Long sponsorshipId) {
        List<Payment> payments = paymentRepository.findBySponsorshipIdOrderByPaymentDateDesc(sponsorshipId);
        return payments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    @Override
    public List<PaymentResponseDto> getPaymentsByStudent(Long studentId) {
        List<Payment> payments = paymentRepository.findByStudentIdOrderByPaymentDateDesc(studentId);
        return payments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
//    public List<PaymentResponseDto> getPaymentsBySponsorship(Long sponsorshipId) {
//        return paymentRepository.findBySponsorshipId(sponsorshipId).stream()
//                .map(this::convertToDto)
//                .collect(Collectors.toList());
//    }

    private PaymentResponseDto convertToDto(Payment payment) {
        PaymentResponseDto dto = modelMapper.map(payment, PaymentResponseDto.class);
        dto.setSponsorshipId(payment.getSponsorship().getId());
        dto.setStudentName(payment.getSponsorship().getStudent().getStudentName());
        dto.setDonorName(payment.getSponsorship().getDonor().getName());

        // Set dates properly
        dto.setStartDate(payment.getStartDate());
        dto.setEndDate(payment.getEndDate());
        dto.setTransactionId(payment.getTransactionId());

        // Set formatted period string
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        dto.setPaidPeriod(String.format("%s - %s",
                payment.getStartDate().format(formatter),
                payment.getEndDate().format(formatter)));

        if (payment.getSponsorship().getStudent().getInstitution() != null) {
            dto.setInstitutionName(payment.getSponsorship().getStudent().getInstitution().getInstitutionName());
        }

        // নতুন fields set করুন
        dto.setReceiptNumber(payment.getReceiptNumber());
        dto.setConfirmedDate(payment.getConfirmedDate());
        dto.setReceivedAmount(payment.getReceivedAmount());
        dto.setConfirmedBy(payment.getConfirmedBy());
        dto.setInstitutionNotes(payment.getInstitutionNotes());

        return dto;
    }

    // Get pending payments for institution
//    @Override
//    public List<SponsorshipResponseDto> getPendingSponsorshipsForInstitution(Long institutionId) {
//        // Find sponsorships that are ACTIVE but have no payments or pending payments
//        List<Sponsorship> pendingSponsorships = sponsorshipRepository
//                .findByStudentInstitutionIdAndStatus(institutionId, SponsorshipStatus.ACTIVE);
//
//        return pendingSponsorships.stream()
//                .map(this::convertToDto)
//                .collect(Collectors.toList());
//    }
// Menual payment confirmation
    @Override
    @Transactional
    public PaymentResponseDto confirmPayment(PaymentConfirmationRequestDto request) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + request.getPaymentId()));

        // Validate payment is pending confirmation
        if (payment.getStatus() != PaymentStatus.PENDING_PAYMENT) {
            throw new BusinessException("Payment is not pending confirmation");
        }

        // Validate required fields
        if (request.getReceiptNumber() == null || request.getReceiptNumber().trim().isEmpty()) {
            throw new BusinessException("Receipt number is required");
        }

        if (request.getReceivedAmount() == null || request.getReceivedAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Valid received amount is required");
        }

        // Update payment with institution confirmation details
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setReceiptNumber(request.getReceiptNumber());

        // Set receipt URL if provided
        if (request.getReceiptUrl() != null && !request.getReceiptUrl().trim().isEmpty()) {
            payment.setReceiptUrl(request.getReceiptUrl());
        }

        payment.setConfirmedDate(LocalDate.now());
        payment.setReceivedAmount(request.getReceivedAmount());
        payment.setInstitutionNotes(request.getNotes());

        // Set receipt date - use provided date or current date
        if (request.getReceiptDate() != null) {
            payment.setReceiptDate(request.getReceiptDate());
        } else {
            payment.setReceiptDate(LocalDate.now());
        }

        // Set received date - use provided date or current date
        if (request.getReceivedDate() != null) {
            payment.setReceivedDate(request.getReceivedDate());
        } else {
            payment.setReceivedDate(LocalDate.now());
        }

        // Set transaction ID if provided
        if (request.getTransactionId() != null && !request.getTransactionId().trim().isEmpty()) {
            payment.setTransactionId(request.getTransactionId());
        }

        // Set confirmed by
        if (request.getConfirmedBy() != null && !request.getConfirmedBy().trim().isEmpty()) {
            payment.setConfirmedBy(request.getConfirmedBy());
        } else {
            // Set default confirmed by (current user/institution name)
            payment.setConfirmedBy("Institution"); // You can get from security context
        }

        // Update sponsorship
        Sponsorship sponsorship = payment.getSponsorship();
        updateSponsorshipAfterConfirmation(sponsorship, payment);

        // Save changes
        paymentRepository.save(payment);
        sponsorshipRepository.save(sponsorship);

        // Send confirmation email
        sendPaymentConfirmationEmail(payment, request.getNotes());

        return convertToDto(payment);
    }

    // Get payments by student and institution
    @Override
    public List<PaymentResponseDto> getPaymentsByStudentAndInstitution(Long studentId, Long institutionId) {
        List<Payment> payments = paymentRepository.findByStudentIdAndInstitutionId(studentId, institutionId);
        return payments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Get payments by donor and institution
    @Override
    public List<PaymentResponseDto> getPaymentsByDonorAndInstitution(Long donorId, Long institutionId) {
        List<Payment> payments = paymentRepository.findByDonorIdAndInstitutionId(donorId, institutionId);
        return payments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private void updateSponsorshipAfterConfirmation(Sponsorship sponsorship, Payment payment) {
        // Update financial totals
        sponsorship.setTotalPaidAmount(sponsorship.getTotalPaidAmount().add(payment.getAmount()));
        sponsorship.setTotalAmount(sponsorship.getTotalAmount().add(payment.getAmount()));
        sponsorship.setLastPaymentDate(LocalDate.now());

        // Extend paidUpTo date
        if (sponsorship.getPaidUpTo() == null ||
                payment.getEndDate().isAfter(sponsorship.getPaidUpTo())) {
            sponsorship.setPaidUpTo(payment.getEndDate());
        }

        // Update sponsorship status
        if (payment.getEndDate().isAfter(sponsorship.getEndDate().minusDays(1))) {
            sponsorship.setStatus(SponsorshipStatus.COMPLETED);
        } else {
            sponsorship.setStatus(SponsorshipStatus.ACTIVE);
        }

        // Update student status
        Student student = sponsorship.getStudent();
        student.updateSponsorshipStatus();
        studentService.updateStudentSponsorshipStatus(student.getStudentId());
    }

    private void sendPaymentConfirmationEmail(Payment payment, String notes) {
        try {
            Donor donor = payment.getSponsorship().getDonor();
            Student student = payment.getSponsorship().getStudent();

            String subject = "Payment Confirmed - LiftAKids";
            String body = buildConfirmationEmailBody(donor, student, payment, notes);

            // Implement your email service here
            // emailService.sendEmail(donor.getEmail(), subject, body);

            System.out.println("Confirmation email would be sent to: " + donor.getEmail());
            System.out.println("Email Body: " + body);

        } catch (Exception e) {
            System.err.println("Failed to send confirmation email: " + e.getMessage());
        }
    }

    private String buildConfirmationEmailBody(Donor donor, Student student, Payment payment, String notes) {
        return String.format("""
            Dear %s,
            
            Your payment has been confirmed by %s institution!
            
            Payment Details:
            - Student: %s
            - Amount: %s Taka
            - Period: %s
            - Months: %d
            - Receipt Number: %s
            - Confirmation Date: %s
            %s
            
            Thank you for your continued support!
            
            Best regards,
            %s Institution
            LiftAKids Team
            """,
                donor.getName(),
                student.getInstitution().getInstitutionName(),
                student.getStudentName(),
                payment.getAmount(),
                payment.getPaidPeriod(),
                payment.getTotalMonths(),
                payment.getReceiptUrl(),
                LocalDate.now().format(DateTimeFormatter.ISO_DATE),
                notes != null ? "Notes: " + notes + "\n" : "",
                student.getInstitution().getInstitutionName()
        );
    }


    // process for manul payment confirmation
    @Override
    @Transactional
    public PaymentResponseDto processInstitutionManualPayment(PaymentRequestDto request) {
        // Validate required fields
        if (request.getStudentId() == null || request.getDonorId() == null ||
                request.getStartDate() == null || request.getEndDate() == null ||
                request.getAmount() == null) {
            throw new BusinessException("Required fields cannot be null");
        }

        // Fetch student and donor
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + request.getStudentId()));

        Donor donor = donorRepository.findById(request.getDonorId())
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found with id: " + request.getDonorId()));

        // Find or create sponsorship - UPDATED LOGIC
        Sponsorship sponsorship;
        if (request.getSponsorshipId() != null) {
            // Use existing sponsorship
            sponsorship = sponsorshipRepository.findById(request.getSponsorshipId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sponsorship not found with id: " + request.getSponsorshipId()));

            // Validate that sponsorship belongs to the same student and donor
            if (!sponsorship.getStudent().getStudentId().equals(request.getStudentId()) ||
                    !sponsorship.getDonor().getDonorId().equals(request.getDonorId())) {
                throw new BusinessException("Sponsorship does not match the provided student and donor");
            }
        } else {
            // Create new sponsorship
            sponsorship = findOrCreateSponsorship(student, donor, request);
        }

        // Adjust dates to month boundaries
        LocalDate adjustedStartDate = request.getStartDate().withDayOfMonth(1);
        LocalDate adjustedEndDate = request.getEndDate().withDayOfMonth(request.getEndDate().lengthOfMonth());

        // Calculate total months
        long totalMonths = ChronoUnit.MONTHS.between(adjustedStartDate, adjustedEndDate) + 1;

        // Validate amount matches monthly amount * months
        BigDecimal monthlyAmount = request.getMonthlyAmount() != null ?
                request.getMonthlyAmount() : student.getRequiredMonthlySupport();

        BigDecimal expectedAmount = monthlyAmount.multiply(BigDecimal.valueOf(totalMonths));
        if (request.getAmount().compareTo(expectedAmount) != 0) {
            throw new BusinessException(String.format(
                    "Payment amount must be %.2f for %d months", expectedAmount, totalMonths));
        }

        // Create COMPLETED payment
        Payment payment = Payment.builder(
                        sponsorship,
                        LocalDate.now(),
                        request.getAmount(),
                        PaymentMethod.MANUAL,
                        PaymentStatus.COMPLETED,
                        adjustedStartDate,
                        adjustedEndDate
                )
                .receiptNumber(request.getReceiptNumber())
                .receiptUrl(request.getReceiptUrl())
                .confirmedDate(LocalDate.now())
                .receivedAmount(request.getReceivedAmount() != null ? request.getReceivedAmount() : request.getAmount())
                .institutionNotes(request.getNotes())
                .transactionId(generateTransactionId())
                .build();

        // Update sponsorship
        updateSponsorshipAfterConfirmation(sponsorship, payment);

        // Save changes
        paymentRepository.save(payment);
        sponsorshipRepository.save(sponsorship);

        // Send confirmation email to donor using simple email service
        try {
            emailService.sendPaymentConfirmationEmail(
                    donor.getEmail(),
                    donor.getName(),
                    student.getStudentName(),
                    payment.getAmount(),
                    payment.getPaidPeriod(),
                    payment.getReceiptNumber()
            );
        } catch (Exception e) {
            log.warn("Failed to send confirmation email, but payment was processed successfully. Error: {}", e.getMessage());
        }

        return convertToDto(payment);
    }
    @Override
    @Transactional
    public PaymentResponseDto createManualPayment(ManualPaymentRequestDto request) {
        // Convert to PaymentRequestDto
        PaymentRequestDto paymentRequest = new PaymentRequestDto();
        paymentRequest.setStudentId(request.getStudentId());
        paymentRequest.setDonorId(request.getDonorId());
        paymentRequest.setStartDate(request.getStartDate());
        paymentRequest.setEndDate(request.getEndDate());
        paymentRequest.setMonthlyAmount(request.getMonthlyAmount());
        paymentRequest.setAmount(request.getAmount());
        paymentRequest.setReceiptNumber(request.getReceiptNumber());
        paymentRequest.setReceiptUrl(request.getReceiptUrl());
        paymentRequest.setNotes(request.getNotes());
        paymentRequest.setPaymentMethod(PaymentMethod.MANUAL);

        // Set receivedAmount - if not provided, use the main amount
        if (request.getReceivedAmount() != null) {
            paymentRequest.setReceivedAmount(request.getReceivedAmount());
        } else {
            paymentRequest.setReceivedAmount(request.getAmount()); // Default to main amount
        }

        // Set sponsorshipId if provided
        if (request.getSponsorshipId() != null) {
            paymentRequest.setSponsorshipId(request.getSponsorshipId());
        }

        // Use existing method
        return processInstitutionManualPayment(paymentRequest);
    }
    private Sponsorship findOrCreateSponsorship(Student student, Donor donor, PaymentRequestDto request) {
        // Try to find existing active sponsorship
        Optional<Sponsorship> existingSponsorship = sponsorshipRepository
                .findByDonorIdAndStudentIdAndStatus(donor.getDonorId(), student.getStudentId(), SponsorshipStatus.ACTIVE);

        if (existingSponsorship.isPresent()) {
            return existingSponsorship.get();
        }

        // Try to find any sponsorship (in case it's pending or inactive)
        List<Sponsorship> anySponsorships = sponsorshipRepository
                .findByDonorIdAndStudentId(donor.getDonorId(), student.getStudentId());

        if (!anySponsorships.isEmpty()) {
            // Take the first one (most recent)
            Sponsorship sponsorship = anySponsorships.get(0);
            sponsorship.setStatus(SponsorshipStatus.ACTIVE);
            sponsorship.setMonthlyAmount(request.getMonthlyAmount() != null ?
                    request.getMonthlyAmount() : student.getRequiredMonthlySupport());
            return sponsorshipRepository.save(sponsorship);
        }
        // Create new sponsorship
        Sponsorship sponsorship = new Sponsorship();
        sponsorship.setDonor(donor);
        sponsorship.setStudent(student);
        sponsorship.setStatus(SponsorshipStatus.ACTIVE);
        sponsorship.setPaymentMethod(PaymentMethod.MANUAL);

        // Set monthly amount
        BigDecimal monthlyAmount = request.getMonthlyAmount() != null ?
                request.getMonthlyAmount() : student.getRequiredMonthlySupport();
        sponsorship.setMonthlyAmount(monthlyAmount);

        sponsorship.setSponsorStartDate(request.getStartDate().withDayOfMonth(1));
        sponsorship.setStartDate(request.getStartDate().withDayOfMonth(1));
        sponsorship.setEndDate(request.getEndDate().withDayOfMonth(request.getEndDate().lengthOfMonth()));
        sponsorship.setTotalPaidAmount(BigDecimal.ZERO);
        sponsorship.setPayments(new ArrayList<>());

        return sponsorshipRepository.save(sponsorship);
    }
   @Override
    public List<PaymentResponseDto> getCompletedPaymentsByInstitutionId(Long institutionId) {
        List<Payment> completedPayments = paymentRepository
                .findBySponsorshipStudentInstitutionInstitutionsIdAndStatus(
                        institutionId, PaymentStatus.COMPLETED);

        return completedPayments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    @Override
    public List<PaymentResponseDto> getPaymentHistoryByStudentId(Long studentId) {
        // শুধু specific student-এর payments return করুন
        return paymentRepository.findBySponsorshipStudentStudentIdAndStatus(
                studentId, PaymentStatus.COMPLETED);
    }

}