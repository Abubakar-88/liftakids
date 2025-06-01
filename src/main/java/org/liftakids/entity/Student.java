package org.liftakids.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
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
    private Long id;

    private String name;
    private Date dob;
    private String gender;
    private String address;
    private String contactNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinancialRank financial_rank;

    private String bio;
    private String photoUrl;
    private boolean sponsored; // sponsor_Id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id")
    private Institutions institution;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<ResultReport> resultReports;


    @OneToMany(mappedBy = "student")
    @Where(clause = "status = 'ACTIVE'") // Filter only active sponsorships
    private List<Sponsorship> currentSponsorships = new ArrayList<>();

    @Column(name = "required_monthly_support", precision = 10, scale = 2)
    private BigDecimal requiredMonthlySupport;



    public BigDecimal getRequiredMonthlySupport() {
        return requiredMonthlySupport != null ? requiredMonthlySupport : BigDecimal.ZERO;
    }

    public void setRequiredMonthlySupport(BigDecimal amount) {
        this.requiredMonthlySupport = amount;
    }
    @Transient
    public boolean isFullySponsored() {
        if (requiredMonthlySupport == null || requiredMonthlySupport.compareTo(BigDecimal.ZERO) <= 0) {
            return !currentSponsorships.isEmpty(); // If no specific amount needed, any sponsorship counts
        }

        BigDecimal totalSponsored = currentSponsorships.stream()
                .map(Sponsorship::getMonthlyAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalSponsored.compareTo(requiredMonthlySupport) >= 0;
    }
}
