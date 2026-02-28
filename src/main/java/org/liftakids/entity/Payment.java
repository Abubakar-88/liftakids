package org.liftakids.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "payments")
@Data
@Builder(builderMethodName = "fullBuilder")
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sponsorship_id", nullable = false)
    private Sponsorship sponsorship;

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Column(name = "paidUpTo")
    private LocalDate paidUpTo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "paymentMethod", length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_months", nullable = false)
    private Integer totalMonths;

    @Column(name = "transaction_id", unique = true)
    private String transactionId;

    @Column(name = "card_last_four")
    private String cardLastFour;

    @Column(name = "payer_email")
    private String payerEmail;

    @Column(name = "payer_phone")
    private String payerPhone;

    @Column(name = "payment_confirmation_message")
    private String confirmationMessage;

    @Column(name = "receipt_url")
    private String receiptUrl;

    @Column(name = "confirmed_date")
    private LocalDate confirmedDate;

    @Column(name = "receipt_number")
    private String receiptNumber;

    @Column(name = "institution_notes")
    private String institutionNotes;

    @Column(name = "received_amount", precision = 10, scale = 2)
    private BigDecimal receivedAmount;

    @Column(name = "confirmed_by")
    private String confirmedBy;

    @Column(name = "received_date")
    private LocalDate receivedDate;

    @Column(name = "receipt_date")
    private LocalDate receiptDate;


    @PrePersist
    @PreUpdate
    private void calculateDerivedFields() {
        // Validate dates
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        // Calculate total months
        if (startDate != null && endDate != null) {
            this.totalMonths = (int) ChronoUnit.MONTHS.between(
                    startDate.withDayOfMonth(1),
                    endDate.withDayOfMonth(1)
            ) + 1;
        }

        // Calculate total amount
//        if (amount != null && totalMonths != null) {
//            this.totalAmount = amount.multiply(new BigDecimal(totalMonths));
//        }

        // Set paidUpTo default to endDate if not set
        if (paidUpTo == null && endDate != null) {
            this.paidUpTo = endDate;
        }
    }

    public static PaymentBuilder builder(Sponsorship sponsorship, LocalDate paymentDate,
                                         BigDecimal amount, PaymentMethod paymentMethod,
                                         PaymentStatus status, LocalDate startDate,
                                         LocalDate endDate) {
        // Calculate derived fields upfront
        int months = (int) ChronoUnit.MONTHS.between(
                startDate.withDayOfMonth(1),
                endDate.withDayOfMonth(1)
        ) + 1;

        return fullBuilder()
                .sponsorship(sponsorship)
                .paymentDate(paymentDate)
                .amount(amount)
                .paymentMethod(paymentMethod)
                .status(status)
                .startDate(startDate)
                .endDate(endDate)
                .paidUpTo(endDate) // Default to end date
                .totalMonths(months)
                .receivedAmount(amount); // Default received amount to original amount
    }
    public void setCardDetails(String fullCardNumber) {
        if (fullCardNumber != null && fullCardNumber.length() >= 4) {
            this.cardLastFour = fullCardNumber.substring(fullCardNumber.length() - 4);
        }
    }

    public String getPaidPeriod() {
        if (startDate == null || endDate == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        return String.format("%s - %s",
                startDate.format(formatter),
                endDate.format(formatter));
    }

}