package org.liftakids.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "sponsorships")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sponsorship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    private Donor donor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "monthly_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyAmount;

    @Column(name = "sponsor_start_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate sponsorStartDate;

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
    @Column(name = "up_date_at")
    private LocalDateTime upDateAT;

    // ========== 🔥 NEW FIELDS FOR CANCEL/REMOVE FUNCTIONALITY ==========

    // When the sponsorship was cancelled
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // Who cancelled the sponsorship (donor ID or admin ID)
    @Column(name = "cancelled_by")
    private Long cancelledBy;

    // Reason for cancellation
    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    // Who removed the sponsorship (if removed by admin)
    @Column(name = "removed_by")
    private Long removedBy;

    // When it was removed
    @Column(name = "removed_at")
    private LocalDateTime removedAt;

    // Flag to indicate if this is a soft delete
    @Column(name = "is_active")
    private Boolean isActive = true;

    // ========== TIMESTAMPS ==========
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @PostRemove
    private void removeFromStudent() {
        if (this.student != null) {
            this.student.removeSponsorship(this);
        }
    }

    @PrePersist
    protected void prePersist() {
        if (this.sponsorStartDate == null) {
            this.sponsorStartDate = LocalDate.now();
        }
        if (isActive == null) {
            isActive = true;
        }
        // 🔥 Set PENDING_PAYMENT as default, not ACTIVE
        if (status == null) {
            status = SponsorshipStatus.PENDING_PAYMENT;  // Changed from ACTIVE
        }
        // Set start date to now if not set
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        // Calculate totals
        calculateTotals();
    }

    @PreUpdate
    private void handleUpdateOperations() {
        // 1. Handle student status updates
        if (this.student != null) {
            // 🔥 Fixed duplicate condition
            if (this.status == SponsorshipStatus.ACTIVE) {
                this.student.addSponsorship(this);
            } else if (this.status == SponsorshipStatus.CANCELLED ||
                    this.status == SponsorshipStatus.REMOVED) {
                this.student.removeSponsorship(this);
            }
            this.student.updateSponsorshipStatus();
        }

        // 2. Calculate totals
        if (startDate != null && endDate != null && monthlyAmount != null) {
            calculateTotals();
        }

        // 3. Fallback to avoid NULLs
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
            this.totalMonths = (int) ChronoUnit.MONTHS.between(
                    startDate.withDayOfMonth(1),
                    endDate.withDayOfMonth(1)
            ) + 1;
            this.totalAmount = this.monthlyAmount.multiply(BigDecimal.valueOf(this.totalMonths));
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

        // Calculate months paid - CORRECTED LOGIC
        this.monthsPaid = calculateActualMonthsPaid();

        // Find the latest paidUpTo date from payments
        this.paidUpTo = this.payments.stream()
                .map(Payment::getPaidUpTo)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(null);

        // Calculate next payment due date
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

        // Calculate payment due status
        if (this.nextPaymentDueDate != null) {
            if (!today.isBefore(this.nextPaymentDueDate)) {
                this.paymentDue = true;
                if (this.nextPaymentDueDate.isBefore(today.minusMonths(1))) {
                    this.overdue = true;
                }
            }
        }

        // Calculate months remaining
        if (this.paidUpTo != null && this.endDate != null) {
            if (this.paidUpTo.isBefore(this.endDate)) {
                this.monthsRemaining = (int) ChronoUnit.MONTHS.between(
                        this.paidUpTo.withDayOfMonth(1),
                        this.endDate.withDayOfMonth(1)
                );
            } else {
                this.monthsRemaining = 0;
            }
        } else {
            this.monthsRemaining = this.totalMonths;
        }

        if (this.student != null) {
            this.student.updateSponsorshipStatus();
        }
    }


    private int calculateActualMonthsPaid() {
        if (this.sponsorStartDate == null || this.paidUpTo == null) {
            return 0;
        }
        long monthsBetween = ChronoUnit.MONTHS.between(
                this.sponsorStartDate.withDayOfMonth(1),
                this.paidUpTo.withDayOfMonth(1)
        ) + 1;
        return Math.max(0, (int) monthsBetween);
    }

    // ========== 🔥 SIMPLIFIED STATUS METHODS ==========

    /**
     * Activate sponsorship - Called after first payment confirmation
     */
    public void activate() {
        if (this.status == SponsorshipStatus.CANCELLED) {
            throw new IllegalStateException("Cannot activate cancelled sponsorship");
        }
        if (this.status == SponsorshipStatus.REMOVED) {
            throw new IllegalStateException("Cannot activate removed sponsorship");
        }
        if (this.status == SponsorshipStatus.ACTIVE) {
            // Already active, no need to change
            return;
        }
        // Only PENDING_PAYMENT can be activated
        if (this.status != SponsorshipStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Cannot activate sponsorship with status: " + this.status);
        }

        this.status = SponsorshipStatus.ACTIVE;
        this.isActive = true;

        if (this.student != null) {
            this.student.addSponsorship(this);
            this.student.updateSponsorshipStatus();
        }
    }

    /**
     * Cancel sponsorship (by donor)
     * @param cancelledBy ID of donor cancelling
     * @param reason Reason for cancellation
     */
    public void cancel(Long cancelledBy, String reason) {
        // Check if already cancelled or removed
        if (this.status == SponsorshipStatus.CANCELLED) {
            throw new IllegalStateException("Sponsorship is already cancelled");
        }
        if (this.status == SponsorshipStatus.REMOVED) {
            throw new IllegalStateException("Sponsorship has been removed");
        }

        // Can cancel ACTIVE or PENDING_PAYMENT sponsorships
        // Allow cancellation for PENDING_PAYMENT, ACTIVE, and EXPIRED
        if (this.status != SponsorshipStatus.ACTIVE &&
                this.status != SponsorshipStatus.PENDING_PAYMENT &&
                this.status != SponsorshipStatus.EXPIRED) {
            throw new IllegalStateException("Cannot cancel sponsorship with status: " + this.status);
        }

        this.status = SponsorshipStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancelledBy = cancelledBy;
        this.cancellationReason = reason;
        this.isActive = false;
        this.endDate = LocalDate.now();

        if (this.student != null) {
            this.student.removeSponsorship(this);
            this.student.updateSponsorshipStatus();
        }
    }

    /**
     * Remove sponsorship (soft delete - admin only)
     */
    public void remove(Long removedBy) {
        this.status = SponsorshipStatus.REMOVED;
        this.removedAt = LocalDateTime.now();
        this.removedBy = removedBy;
        this.isActive = false;

        if (this.student != null) {
            this.student.removeSponsorship(this);
            this.student.updateSponsorshipStatus();
        }
    }

    /**
     * Check if sponsorship can be cancelled
     */
    public boolean canBeCancelled() {
        // Already cancelled or removed cannot be cancelled again
        if (this.status == SponsorshipStatus.CANCELLED ||
                this.status == SponsorshipStatus.REMOVED) {
            return false;
        }
        // Only ACTIVE and PENDING_PAYMENT can be cancelled
        return this.status == SponsorshipStatus.ACTIVE ||
                this.status == SponsorshipStatus.PENDING_PAYMENT ||
                this.status == SponsorshipStatus.EXPIRED;
    }
    /**
     * Check if sponsorship is pending payment
     */
    public boolean isPendingPayment() {
        return this.status == SponsorshipStatus.PENDING_PAYMENT;
    }

    /**
     * Check if sponsorship is active
     */
    public boolean isActive() {
        return this.status == SponsorshipStatus.ACTIVE && this.isActive;
    }

    /**
     * Get status display name
     */
    public String getStatusDisplayName() {
        switch (this.status) {
            case PENDING_PAYMENT:
                return "Pending Payment";
            case ACTIVE:
                return "Active";
            case CANCELLED:
                return "Cancelled";
            case REMOVED:
                return "Removed";
            case EXPIRED:
                return "Expired";
            default:
                return this.status.name();
        }
    }
    /**
     * Check if sponsorship is active
     */
    public boolean isActiveSponsorship() {
        return this.isActive && this.status == SponsorshipStatus.ACTIVE;
    }
}