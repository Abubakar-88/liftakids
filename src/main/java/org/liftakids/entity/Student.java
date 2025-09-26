package org.liftakids.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studentId;

    private String studentName;
    private Date dob;
    private String gender;
    private String address;
    private String contactNumber;

    @Column(name = "class_name")
    private String className;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinancialRank financial_rank;

    private String bio;
    private String photoUrl;


    private String guardianName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id")
    private Institutions institution;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<ResultReport> resultReports;


    @Column(name = "is_sponsored", nullable = false)
    private boolean isSponsored = false;

    @OneToMany(mappedBy = "student")
    @Where(clause = "status = 'COMPLETED'")
    private List<Sponsorship> currentSponsorships = new ArrayList<>();

    @Column(name = "required_monthly_support", precision = 10, scale = 2)
    private BigDecimal requiredMonthlySupport;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudentStatus status = StudentStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "last_updated_date")
    private LocalDateTime lastUpdatedDate;

    @Transient
    public BigDecimal getTotalSponsoredAmount() {
        return currentSponsorships.stream()
                .map(Sponsorship::getTotalPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transient
    public boolean isFullySponsored() {
        if (requiredMonthlySupport == null || requiredMonthlySupport.compareTo(BigDecimal.ZERO) <= 0) {
            return isSponsored();
        }
        return getTotalSponsoredAmount().compareTo(requiredMonthlySupport) >= 0;
    }


    public boolean isSponsored() {
        return this.isSponsored;
    }

    // Add synchronization methods
    public void addSponsorship(Sponsorship sponsorship) {
        if (!this.currentSponsorships.contains(sponsorship)) {
            this.currentSponsorships.add(sponsorship);
            this.isSponsored = true;
        }
    }

    public void removeSponsorship(Sponsorship sponsorship) {
        this.currentSponsorships.remove(sponsorship);
        this.isSponsored = !this.currentSponsorships.isEmpty();
    }

    @PostLoad
    public void updateSponsorshipStatus() {
        boolean newStatus = !this.currentSponsorships.isEmpty();
        if (this.isSponsored != newStatus) {
            this.isSponsored = newStatus;
        }
    }
    public void refreshSponsorshipStatus() {
        this.isSponsored = !this.currentSponsorships.isEmpty();
    }
    public BigDecimal getRequiredMonthlySupport() {
        return requiredMonthlySupport != null ? requiredMonthlySupport : BigDecimal.ZERO;
    }

    public void setRequiredMonthlySupport(BigDecimal amount) {
        this.requiredMonthlySupport = amount;
    }
//    @Transient
//    public boolean isFullySponsored() {
//        if (requiredMonthlySupport == null || requiredMonthlySupport.compareTo(BigDecimal.ZERO) <= 0) {
//            return !currentSponsorships.isEmpty(); // If no specific amount needed, any sponsorship counts
//        }
//
//        BigDecimal totalSponsored = currentSponsorships.stream()
//                .map(Sponsorship::getTotalPaidAmount)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        return totalSponsored.compareTo(requiredMonthlySupport) >= 0;
//    }
}
