package org.liftakids.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.liftakids.entity.enm.NotificationStatus;
import org.liftakids.entity.enm.NotificationType;
import org.liftakids.entity.enm.UserType;

import java.time.LocalDateTime;
@Entity
//@Table(name = "notifications", indexes = {
//        @Index(name = "idx_notification_recipient", columnList = "userType, userId"),
//        @Index(name = "idx_notification_status", columnList = "status"),
//        @Index(name = "idx_notification_created", columnList = "createdAt DESC")
//})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    // ============= RECIPIENT INFORMATION =============
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", length = 20)
    private UserType userType; // DONOR, INSTITUTION, ADMIN, SYSTEM

    @Column(name = "user_id")
    private Long userId; // DonorId, InstitutionId, AdminId

    // Optional: Keep old relationships for backward compatibility
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = true)
    private Donor donor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = true)
    private Institutions institution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = true)
    private SystemAdmin admin;

    // ============= NOTIFICATION CONTENT =============
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(length = 500)
    private String shortMessage; // For SMS or push notifications

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    // ============= STATUS & METADATA =============
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.UNREAD;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime readAt;
    private LocalDateTime sentAt;
    private LocalDateTime archivedAt;

    // ============= ACTION & NAVIGATION =============
    @Column(length = 500)
    private String actionUrl;

    @Column(length = 100)
    private String actionText; // "View Details", "Approve Now", etc.

    @Column(length = 50)
    private String icon; // "success", "warning", "info", "error"

    // ============= RELATED ENTITY REFERENCES =============
    @Column(length = 50)
    private String relatedEntityType; // "SPONSORSHIP", "PAYMENT", "INSTITUTION", "DONOR"

    private Long relatedEntityId;

    @Column(length = 100)
    private String relatedEntityName; // For display purposes

    // ============= DELIVERY STATUS =============
    private boolean emailSent = false;
    private boolean smsSent = false;
    private boolean pushSent = false;
    private boolean inAppSent = true; // Always true for database notifications

    @Column(length = 1000)
    private String emailError; // If email fails
    private LocalDateTime emailSentAt;

    // ============= PRIORITY & CATEGORY =============
    @Column(length = 20)
    private String priority; // "HIGH", "MEDIUM", "LOW"

    @Column(length = 50)
    private String category; // "FINANCIAL", "SECURITY", "SYSTEM", "USER"

    @Column(length = 100)
    private String tags; // Comma separated: "payment,urgent,monthly"

    // ============= SENDER INFORMATION =============
    @Column(length = 100)
    private String senderName; // "System", "Support Team"

    @Column(length = 100)
    private String senderType; // "SYSTEM", "ADMIN", "DONOR", "INSTITUTION"

    private Long senderId;

    // ============= ADDITIONAL DATA =============
    @Column(columnDefinition = "JSON")
    private String metadata; // JSON formatted additional data

    @Version
    private Long version;

    // ============= HELPER METHODS =============
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = NotificationStatus.UNREAD;
        }
        if (inAppSent) {
            sentAt = LocalDateTime.now();
        }
    }

    public boolean isForDonor() {
        return userType == UserType.DONOR || donor != null;
    }

    public boolean isForInstitution() {
        return userType == UserType.INSTITUTION || institution != null;
    }

    public boolean isForAdmin() {
        return userType == UserType.ADMIN || admin != null;
    }

    public void markAsRead() {
        this.status = NotificationStatus.READ;
        this.readAt = LocalDateTime.now();
    }

    public void markAsArchived() {
        this.status = NotificationStatus.ARCHIVED;
        this.archivedAt = LocalDateTime.now();
    }

    public void markEmailSent(boolean success, String error) {
        this.emailSent = success;
        this.emailSentAt = success ? LocalDateTime.now() : null;
        this.emailError = error;
    }
}
