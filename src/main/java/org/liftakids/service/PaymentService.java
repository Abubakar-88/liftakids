package org.liftakids.service;

import org.liftakids.dto.payment.ManualPaymentRequestDto;
import org.liftakids.dto.payment.PaymentConfirmationRequestDto;
import org.liftakids.dto.payment.PaymentRequestDto;
import org.liftakids.dto.payment.PaymentResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PaymentService {
    PaymentResponseDto processPayment(PaymentRequestDto request);
    List<PaymentResponseDto> getPaymentsBySponsorship(Long sponsorshipId);
    public List<PaymentResponseDto> getPaymentsByStudent(Long studentId);
    Page<PaymentResponseDto> getPaymentsByDonor(Long donorId, int page, int size);
    List<PaymentResponseDto> getPaymentsByDonor(Long donorId);

    // New methods for institution payment confirmation
   // List<PaymentResponseDto> getPendingPaymentsForInstitution(Long institutionId);
    PaymentResponseDto confirmPayment(PaymentConfirmationRequestDto request);
    List<PaymentResponseDto> getPaymentsByStudentAndInstitution(Long studentId, Long institutionId);
    List<PaymentResponseDto> getPaymentsByDonorAndInstitution(Long donorId, Long institutionId);

    //menual payment
   PaymentResponseDto processInstitutionManualPayment(PaymentRequestDto request);
    PaymentResponseDto createManualPayment(ManualPaymentRequestDto request);

    List<PaymentResponseDto> getCompletedPaymentsByInstitutionId(Long institutionId);

    List<PaymentResponseDto> getPaymentHistoryByStudentId(Long studentId);
}
