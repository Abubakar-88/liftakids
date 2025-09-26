package org.liftakids.dto.sponsorship;

import lombok.*;
import org.liftakids.entity.PaymentMethod;
import org.liftakids.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
public class PaymentResponseDto {
    private Long id;
    private Long sponsorshipId;
    private String studentName;
    private String donorName;
    private String institutionName;
    private LocalDate paymentDate;
    private String paidPeriod;  // This is the field we need to set
    private String paidUpTo;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String cardLastFour;  // Last 4 digits if credit card
    private String transactionId;
    // Add these if needed
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalMonths;

    public String getPaymentPeriod() {
        if (startDate != null && endDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
            return String.format("%s - %s",
                    startDate.format(formatter),
                    endDate.format(formatter));
        }
        return "";
    }
}