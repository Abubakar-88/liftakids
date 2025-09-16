package org.liftakids.dto.student;

import lombok.Builder;
import lombok.Data;
import org.liftakids.entity.SponsorshipStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
public class StudentResponseDto {
    private Long studentId;
    private String studentName;
    private Date dob;
    private String gender;
    private String address;
    private String contactNumber;
    private String financial_rank;
    private String bio;
    private String photoUrl;
    private boolean sponsored;
    private String guardianName;
    private Long institutionsId;
    private String institutionName;
    private String institutionPhone;
    private BigDecimal requiredMonthlySupport;
    private boolean fullySponsored;
    private BigDecimal sponsoredAmount;
    private List<SponsorInfoDto> sponsors;

    @Data
    @Builder
    public static class SponsorInfoDto {
        private Long donorId;
        private String donorName;
        private BigDecimal monthlyAmount;
        private Integer totalMonths;
        private LocalDate startDate;
        private LocalDate endDate;
        private SponsorshipStatus status;
        private LocalDate lastPaymentDate;
        private LocalDate paidUpTo;
        private Integer monthsPaid;
        private BigDecimal totalAmount;
        private LocalDate nextPaymentDueDate;
    }

}
