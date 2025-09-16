package org.liftakids.entity;

import jakarta.persistence.*;
import lombok.*;
import org.liftakids.exception.BusinessException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "sponsorships")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sponsorship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    private Donor donor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "monthly_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyAmount;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;  // Initialize with default value

    @Column(name = "total_months", nullable = false)
    private Integer totalMonths = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "total_paid_amount", precision = 10, scale = 2)
    private BigDecimal totalPaidAmount = BigDecimal.ZERO;

    @Column(name = "paid_up_to")
    private LocalDate paidUpTo;

    @Column(name = "last_payment_date")
    private LocalDate lastPaymentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SponsorshipStatus status;

    @OneToMany(mappedBy = "sponsorship", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    @Transient
    private boolean paymentDue;

    @Transient
    private boolean overdue;

    @Transient
    private LocalDate nextPaymentDueDate;

    @Transient
    private Integer monthsPaid;

    @Transient
    private Integer monthsRemaining;


    @PostRemove
    private void removeFromStudent() {
        if (this.student != null) {
            this.student.removeSponsorship(this);
        }
    }

    @PreUpdate
    @PrePersist
    private void handleUpdateOperations() {
        // 1. Handle student status updates
        if (this.student != null) {
            if (this.status == SponsorshipStatus.ACTIVE ||
                    this.status == SponsorshipStatus.COMPLETED) {
                this.student.addSponsorship(this);
            } else if (this.status == SponsorshipStatus.CANCELLED ||
                    this.status == SponsorshipStatus.EXPIRED) {
                this.student.removeSponsorship(this);
            }
            // Force immediate status update
            this.student.updateSponsorshipStatus();
        }

        // 2. Calculate totals if possible
        if (startDate != null && endDate != null && monthlyAmount != null) {
            this.totalMonths = (int) ChronoUnit.MONTHS.between(
                    startDate.withDayOfMonth(1),
                    endDate.withDayOfMonth(1)
            ) + 1;
            this.totalAmount = monthlyAmount.multiply(BigDecimal.valueOf(totalMonths));
        }

        // 3. Fallback to avoid NULLs in DB
        if (totalMonths == null) totalMonths = 0;
        if (totalAmount == null) totalAmount = BigDecimal.ZERO;
    }

    public void setDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        this.startDate = startDate;
        this.endDate = endDate;
        calculateTotals();
    }

    private void calculateTotals() {
        if (this.startDate != null && this.endDate != null && this.monthlyAmount != null) {
            this.totalMonths = (int) ChronoUnit.MONTHS.between(startDate, endDate) + 1;
            this.totalAmount = this.monthlyAmount.multiply(new BigDecimal(this.totalMonths));
        }
    }

    @PostLoad
    @PostUpdate
    @PostPersist
    private void calculatePaymentStatus() {
        if (this.startDate == null || this.endDate == null) return;

        LocalDate today = LocalDate.now();
        this.paymentDue = false;
        this.overdue = false;

        // Calculate months paid
        this.monthsPaid = this.payments.stream()
                .mapToInt(p -> p.getTotalMonths() != null ? p.getTotalMonths() : 0)
                .sum();

        // Find the latest paidUpTo date from payments
        this.paidUpTo = this.payments.stream()
                .map(Payment::getPaidUpTo)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(null);

        // Calculate next payment due date - NEW IMPROVED LOGIC
        if (this.paidUpTo != null) {
            if (this.paidUpTo.isBefore(this.endDate)) {
                this.nextPaymentDueDate = this.paidUpTo.plusMonths(1);
            } else {
                // Fully paid - no next payment due
                this.nextPaymentDueDate = null;
            }
        } else {
            // No payments made yet
            if (!today.isBefore(this.startDate)) {
                // Sponsorship has started or starts today
                this.nextPaymentDueDate = this.startDate;
            } else {
                // Future sponsorship
                this.nextPaymentDueDate = this.startDate;
            }
        }

        // Calculate payment due status - NEW IMPROVED LOGIC
        if (this.nextPaymentDueDate != null) {
            if (!today.isBefore(this.nextPaymentDueDate)) {
                this.paymentDue = true;
                if (this.nextPaymentDueDate.isBefore(today.minusMonths(1))) {
                    this.overdue = true;
                }
            }
        }
        if (this.student != null) {
            this.student.updateSponsorshipStatus();
        }
    }
    public void setStatus(SponsorshipStatus newStatus) {
        SponsorshipStatus oldStatus = this.status;
        this.status = newStatus;

        if (this.student != null && !newStatus.equals(oldStatus)) {
            if (newStatus == SponsorshipStatus.ACTIVE ||
                    newStatus == SponsorshipStatus.COMPLETED) {
                this.student.addSponsorship(this);
            } else {
                this.student.removeSponsorship(this);
            }
            this.student.updateSponsorshipStatus();
        }
    }
}