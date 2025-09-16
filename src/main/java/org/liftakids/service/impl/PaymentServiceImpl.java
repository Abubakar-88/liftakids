package org.liftakids.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.liftakids.dto.sponsorship.PaymentRequestDto;
import org.liftakids.dto.sponsorship.PaymentResponseDto;
import org.liftakids.entity.*;
import org.liftakids.exception.BusinessException;
import org.liftakids.exception.ResourceNotFoundException;
import org.liftakids.repositories.PaymentRepository;
import org.liftakids.repositories.SponsorshipRepository;
import org.liftakids.service.PaymentService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final SponsorshipRepository sponsorshipRepository;
    private final ModelMapper modelMapper;
    private final StudentServiceImpl studentService;
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
        if (adjustedStartDate.isBefore(sponsorship.getStartDate())) {
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

        // Set formatted period string
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        dto.setPaidPeriod(String.format("%s - %s",
                payment.getStartDate().format(formatter),
                payment.getEndDate().format(formatter)));

        if (payment.getSponsorship().getStudent().getInstitution() != null) {
            dto.setInstitutionName(payment.getSponsorship().getStudent().getInstitution().getInstitutionName());
        }

        return dto;
    }



}