package org.liftakids.dto.sponsorship;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.liftakids.dto.payment.PaymentResponseDto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingPaymentDashboardResponseDto {

    private List<SponsorshipResponseDto> pendingSponsorships;

    private List<PaymentResponseDto> pendingPayments;
}