package org.liftakids.dto.sponsorship;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.CreditCardNumber;
import org.liftakids.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class PaymentRequestDto {
    @NotNull(message = "Sponsorship ID is required")
    private Long sponsorshipId;

    @NotNull(message = "Start date is required")
    @JsonDeserialize(using = MonthYearDeserializer.class)
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @JsonDeserialize(using = MonthYearDeserializer.class)
    private LocalDate endDate;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Digits(integer = 10, fraction = 2, message = "Amount must have up to 10 integer and 2 fraction digits")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Valid
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private CardDetails cardDetails;

    private boolean testPayment = false;

    @Data
    public static class CardDetails {
        @NotBlank(groups = CreditCardValidation.class)
        @CreditCardNumber(groups = CreditCardValidation.class)
        private String cardNumber;

        @NotBlank(groups = CreditCardValidation.class)
        @Pattern(regexp = "^(0[1-9]|1[0-2])$", groups = CreditCardValidation.class)
        private String expiryMonth;

        @NotBlank(groups = CreditCardValidation.class)
        @Pattern(regexp = "^20[2-9][0-9]$", groups = CreditCardValidation.class)
        private String expiryYear;

        @NotBlank(groups = CreditCardValidation.class)
        @Pattern(regexp = "^[0-9]{3,4}$", groups = CreditCardValidation.class)
        private String cvv;

        @NotBlank(groups = CreditCardValidation.class)
        private String cardHolderName;
    }

    public interface CreditCardValidation {}

    @AssertTrue(message = "Card details are required for credit card payments")
    public boolean isCardDetailsValid() {
        return paymentMethod != PaymentMethod.CREDIT_CARD || cardDetails != null;
    }

    @AssertTrue(message = "End date must be after start date")
    public boolean isDateRangeValid() {
        return endDate == null || startDate == null || endDate.isAfter(startDate);
    }

    public LocalDate getStartDate() {
        return startDate != null ? startDate.withDayOfMonth(1) : null;
    }

    public LocalDate getEndDate() {
        return endDate != null ? endDate.withDayOfMonth(endDate.lengthOfMonth()) : null;
    }

    // Setters
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate != null ? startDate.withDayOfMonth(1) : null;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate != null ? endDate.withDayOfMonth(endDate.lengthOfMonth()) : null;
    }

}
