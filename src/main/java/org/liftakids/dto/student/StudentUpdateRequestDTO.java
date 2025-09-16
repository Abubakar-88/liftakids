package org.liftakids.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

public record StudentUpdateRequestDTO(
        @NotBlank String studentName,
        @NotBlank String contactNumber,
        @NotNull Date dob,
        String bio,
        @NotBlank String financial_rank,
        @PositiveOrZero BigDecimal requiredMonthlySupport,
        @NotBlank String address,
        @NotBlank String guardianName
) {}