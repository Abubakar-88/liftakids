package org.liftakids.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ManualPaymentRequestDto {
    @NotNull
    private Long studentId;
    private Long sponsorshipId;
    @NotNull
    private Long donorId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    private BigDecimal monthlyAmount;
    private String paymentMethod;
    @NotNull
    private BigDecimal amount;
    private BigDecimal receivedAmount;
    @NotNull
    private String receiptNumber;

    private String receiptUrl;

    private String notes;
}
