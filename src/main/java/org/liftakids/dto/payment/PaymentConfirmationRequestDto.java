package org.liftakids.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentConfirmationRequestDto {
    @NotNull
    private Long paymentId;

    private String receiptNumber;
    private LocalDate receiptDate;
    private BigDecimal receivedAmount;
    private LocalDate receivedDate;
    private String transactionId;
    private String notes;
    private String confirmedBy;
    private String receiptUrl;
}