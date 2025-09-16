package org.liftakids.service;

import org.liftakids.dto.sponsorship.PaymentRequestDto;
import org.liftakids.dto.sponsorship.PaymentResponseDto;
import org.liftakids.dto.sponsorship.SponsorshipResponseDto;

import java.util.List;

public interface PaymentService {
    PaymentResponseDto processPayment(PaymentRequestDto request);
    List<PaymentResponseDto> getPaymentsBySponsorship(Long sponsorshipId);
    public List<PaymentResponseDto> getPaymentsByStudent(Long studentId);
}
