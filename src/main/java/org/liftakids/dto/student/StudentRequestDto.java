package org.liftakids.dto.student;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.liftakids.entity.FinancialRank;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class StudentRequestDto {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50)
    private String studentName;

    @NotNull(message = "Date of birth is required")
    private Date dob;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Contact number is required")
    private String contactNumber;

    @NotNull(message = "Financial rank is required")
    private FinancialRank financial_rank;

    @Size(max = 1000, message = "Bio must be less than 1000 characters")
    private String bio;

    private MultipartFile image;

    @NotBlank(message = "Guardian name is required")
    private String guardianName;

    @NotNull(message = "Institutions ID is required")
    private Long institutionsId;

    @DecimalMin(value = "0.0", inclusive = true, message = "Required monthly support must be non-negative")
    private BigDecimal requiredMonthlySupport;
}
