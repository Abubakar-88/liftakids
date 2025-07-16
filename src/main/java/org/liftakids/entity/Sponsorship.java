package org.liftakids.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "sponsor")
public class Sponsorship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sponsorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    private Donor donor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_interval", nullable = false)
    private PaymentInterval paymentInterval;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "amount_per_month", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPerMonth;

    @Column(name = "paid_up_to")
    private LocalDate paidUpTo;

    @Enumerated(EnumType.STRING)
    private SponsorshipStatus status;

    public boolean isActive() {
        return status == SponsorshipStatus.ACTIVE;
    }

}
