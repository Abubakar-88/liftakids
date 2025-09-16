package org.liftakids.dto.sponsorship;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.liftakids.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
@Data
public class SponsorshipRequestDto {
    @NotNull(message = "Donor ID is required")
    private Long donorId;

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Start date is required")
    @PastOrPresent(message = "Start date can be current or past date")
    @JsonDeserialize(using = MonthYearDeserializer.class)
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @JsonDeserialize(using = MonthYearDeserializer.class)
    private LocalDate endDate;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Positive(message = "Monthly amount must be positive")
    @Digits(integer = 10, fraction = 2, message = "Monthly amount must have up to 10 integer and 2 fraction digits")
    private BigDecimal monthlyAmount;

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
    public Integer getTotalMonths() {
        if (startDate == null || endDate == null) return 0;
        return (int) ChronoUnit.MONTHS.between(
                startDate.withDayOfMonth(1),
                endDate.withDayOfMonth(1)
        ) + 1;
    }
}
