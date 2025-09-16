package org.liftakids.dto.sponsorship;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.liftakids.entity.FinancialRank;
import org.liftakids.entity.PaymentMethod;
import org.liftakids.entity.SponsorshipStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SponsorshipResponseDto {
    private Long id;
    private String donorName;
    private String studentName;
    private Long studentId;
    private String address;
    private String contactNumber;
    private String guardianName;
    private String photoUrl;
    private String bio;
    private String institutionName;
    private FinancialRank financial_rank;
    private BigDecimal monthlyAmount;
    private BigDecimal totalAmount;
    private LocalDate paidUpTo;
    private BigDecimal totalPaidAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextPaymentDueDate;
    private LocalDate lastPaymentDate;
    private Integer totalMonths;
    private Integer monthsPaid;
    private Integer totalPayments;
    private PaymentMethod paymentMethod;
    private SponsorshipStatus status;
    private String periodDisplay;
    private String paymentMethodDisplay;
    private boolean paymentDue;
    private boolean overdue;
    private String message;
    private SponsorDetailsDto sponsorDetails;
    private boolean sponsored;
    private StudentSponsorshipStatusDto studentStatus;


    @Data
    @Builder
    public static class StudentSponsorshipStatusDto {
        private boolean fullySponsored;

        private BigDecimal requiredAmount;
        private BigDecimal sponsoredAmount;
        private LocalDate lastPaymentDate;
        private LocalDate paidUpTo;
    }

}