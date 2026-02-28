package org.liftakids.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.liftakids.entity.address.Districts;
import org.liftakids.entity.address.Divisions;
import org.liftakids.entity.address.Thanas;
import org.liftakids.entity.address.UnionOrArea;
import org.liftakids.entity.enm.InstitutionStatus;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "institutions")
public class Institutions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long institutionsId;

    @Column(name = "institution_name", nullable = false)
    private String institutionName;

    // Location hierarchy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "division_id", nullable = false)
    private Divisions division;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", nullable = false)
    private Districts district;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thana_id", nullable = false)
    private Thanas thana;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "union_or_area_id", nullable = false)
    private UnionOrArea unionOrArea;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstitutionType type;

    @Column(name ="teacher_name" ,nullable = false)
    private String teacherName;

    @Column(name = "teacher_designation", nullable = false)
    private String teacherDesignation;

    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(name = "village_or_house")
    private String villageOrHouse;

    @Column(nullable = false)
    private String password;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private InstitutionStatus status = InstitutionStatus.PENDING;

    // Approval fields
    @Column(name = "is_approved")
    private Boolean isApproved = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private SystemAdmin approvedBy;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Column(name = "approval_notes", columnDefinition = "TEXT")
    private String approvalNotes;

    // Rejection fields
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejected_by")
    private SystemAdmin rejectedBy;

    @Column(name = "rejection_date")
    private LocalDateTime rejectionDate;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    // suspended fields
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suspended_by")
    private SystemAdmin suspendedBy;

    @Column(name = "suspended_date")
    private LocalDateTime suspendedDate;

    @Column(name = "suspended_reason", columnDefinition = "TEXT")
    private String suspendedReason;

    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @Column(name = "about_institution", nullable = false, length = 1500)
    private String aboutInstitution;

    @OneToMany(mappedBy = "institution", cascade = CascadeType.ALL)
    @JsonBackReference
    private List<Student> students;

    // Helper methods
    @PrePersist
    protected void onCreate() {
        if (registrationDate == null) {
            registrationDate = LocalDateTime.now();
        }
        if (updateDate == null) {
            updateDate = LocalDateTime.now();
        }
        if (isApproved == null) {
            isApproved = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateDate = LocalDateTime.now();
    }

    public void approve(SystemAdmin admin, String notes) {
        this.isApproved = true;

        // ✅ CRITICAL: Use the enum constant, NOT a string
        this.status = InstitutionStatus.APPROVED;  // Make sure this is the enum

        this.approvedBy = admin;
        this.approvalDate = LocalDateTime.now();
        this.approvalNotes = notes;

        // Clear rejection fields
        this.rejectedBy = null;
        this.rejectionDate = null;
        this.rejectionReason = null;

        // Clear suspension fields if any
        this.suspendedBy = null;
        this.suspendedDate = null;
        this.suspendedReason = null;
    }
    public void reject(SystemAdmin admin, String reason) {
        this.isApproved = false;
        this.status = InstitutionStatus.REJECTED;
        this.rejectedBy = admin;
        this.rejectionDate = LocalDateTime.now();
        this.rejectionReason = reason;
        this.approvedBy = null;
        this.approvalDate = null;
        this.approvalNotes = null;
    }
}
