package org.liftakids.service;

import org.liftakids.dto.sponsorship.PaymentRequestDto;
import org.liftakids.dto.sponsorship.PaymentResponseDto;
import org.liftakids.dto.sponsorship.SponsorshipResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PaymentService {
    PaymentResponseDto processPayment(PaymentRequestDto request);
    List<PaymentResponseDto> getPaymentsBySponsorship(Long sponsorshipId);
    public List<PaymentResponseDto> getPaymentsByStudent(Long studentId);
    Page<PaymentResponseDto> getPaymentsByDonor(Long donorId, int page, int size);
    List<PaymentResponseDto> getPaymentsByDonor(Long donorId);
}
