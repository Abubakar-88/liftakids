package org.liftakids.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.liftakids.entity.*;
import org.liftakids.entity.enm.NotificationStatus;
import org.liftakids.entity.enm.NotificationType;
import org.liftakids.entity.enm.UserType;
import org.liftakids.repositories.AdminRepository;
import org.liftakids.repositories.NotificationRepository;
import org.liftakids.service.NotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final AdminRepository adminRepository;
    // ============= EXISTING DONOR METHODS =============
    @Override
    public List<Notification> getDonorNotifications(Long donorId) {
        return notificationRepository.findByDonorDonorIdOrderByCreatedAtDesc(donorId);
    }

    @Override
    public List<Notification> getUnreadNotifications(Long donorId) {
        return notificationRepository.findByDonorDonorIdAndStatusOrderByCreatedAtDesc(
                donorId, NotificationStatus.UNREAD);
    }

    @Override
    public Long getUnreadCount(Long donorId) {
        return notificationRepository.countByDonorDonorIdAndStatus(
                donorId, NotificationStatus.UNREAD);
    }

    @Transactional
    @Override
    public void markAsRead(Long notificationId, Long donorId) {
        int updated = notificationRepository.markAsReadForDonor(
                notificationId,
                donorId,
                NotificationStatus.READ,
                LocalDateTime.now()
        );

        if (updated == 0) {
            log.warn("Notification {} not found or not owned by donor {}",
                    notificationId, donorId);
        }
    }

    @Transactional
    @Override
    public void markAllAsRead(Long donorId) {
        int updated = notificationRepository.markAllAsReadForDonor(
                donorId,
                NotificationStatus.READ,
                LocalDateTime.now()
        );

        log.info("Marked {} notifications as read for donor {}", updated, donorId);
    }

    // ============= NEW INSTITUTION METHODS =============
    @Override
    public List<Notification> getInstitutionNotifications(Long institutionsId) {
        return notificationRepository.findByInstitutionInstitutionsIdOrderByCreatedAtDesc(institutionsId);
    }

    @Override
    public List<Notification> getUnreadNotificationsForInstitution(Long institutionsId) {
        return notificationRepository.findByInstitutionInstitutionsIdAndStatusOrderByCreatedAtDesc(
                institutionsId, NotificationStatus.UNREAD);
    }

    @Override
    public Long getUnreadCountForInstitution(Long institutionId) {
        return notificationRepository.countByInstitutionInstitutionsIdAndStatus(
                institutionId, NotificationStatus.UNREAD);
    }

    @Transactional
    @Override
    public void markAsReadForInstitution(Long notificationId, Long institutionId) {
        int updated = notificationRepository.markAsReadForInstitution(
                notificationId,
                institutionId,
                NotificationStatus.READ,
                LocalDateTime.now()
        );

        if (updated == 0) {
            log.warn("Notification {} not found or not owned by institution {}",
                    notificationId, institutionId);
        }
    }

    @Transactional
    @Override
    public void markAllAsReadForInstitution(Long institutionId) {
        int updated = notificationRepository.markAllAsReadForInstitution(
                institutionId,
                NotificationStatus.READ,
                LocalDateTime.now()
        );

        log.info("Marked {} notifications as read for institution {}", updated, institutionId);
    }
    @Override
    @Transactional
    public void sendInstitutionRegistrationNotification(Institutions institution) {
        try {
            log.info("Sending registration notification for institution: {}",
                    institution.getInstitutionName());

            // 1. Notification to institution
            createInstitutionNotification(
                    institution,
                    "Registration Submitted",
                    String.format("Dear %s, your registration request has been submitted successfully. Our admin team will review it shortly.",
                            institution.getInstitutionName()),
                    NotificationType.INSTITUTION_REGISTRATION,
                    "/institutions/dashboard",
                    "INSTITUTION",
                    institution.getInstitutionsId()
            );

            // 2. Notification to all admins
            sendNotificationToAllAdmins(
                    "New Institution Registration",
                    String.format("Institution '%s' has submitted registration request. Please review.",
                            institution.getInstitutionName()),
                    NotificationType.ADMIN_ALERT,
                    "/admin/institutions/pending",
                    "INSTITUTION",
                    institution.getInstitutionsId()
            );

            log.info("✅ Registration notifications sent successfully for institution: {}",
                    institution.getInstitutionName());

        } catch (Exception e) {
            log.error("❌ Failed to send registration notifications for institution {}: {}",
                    institution.getInstitutionName(), e.getMessage(), e);
            // Don't throw exception - notification failure shouldn't break registration
        }
    }
    @Override
    @Transactional
    public Notification createDonorNotification(Donor donor, String title, String message,
                                                NotificationType type, String actionUrl,
                                                String relatedEntityType, Long relatedEntityId) {

        log.info("Creating donor notification for donorId: {}", donor.getDonorId());

        Notification notification = Notification.builder()
                .userType(UserType.DONOR)
                .userId(donor.getDonorId())
                .donor(donor)
                .title(title)
                .message(message)
                .type(type)
                .status(NotificationStatus.UNREAD)
                .createdAt(LocalDateTime.now())
                .inAppSent(true)
                .actionUrl(actionUrl)
                .actionText(getActionTextByType(type))
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .senderName("System")
                .senderType("SYSTEM")
                .priority(getPriorityByType(type))
                .category(getCategoryByType(type))
                .metadata(createDonorMetadata(donor, relatedEntityId))
                .emailSent(true).smsSent(true).pushSent(true).build();

        return notificationRepository.save(notification);
    }

    // ============ INSTITUTION NOTIFICATION ============
    @Override
    @Transactional
    public Notification createInstitutionNotification(Institutions institution, String title, String message,
                                                      NotificationType type, String actionUrl,
                                                      String relatedEntityType, Long relatedEntityId) {

        log.info("Creating institution notification for institutionId: {}", institution.getInstitutionsId());

        Notification notification = Notification.builder()
                .userType(UserType.INSTITUTION)
                .userId(institution.getInstitutionsId())
                .institution(institution)
                .title(title)
                .message(message)
                .type(type)
                .status(NotificationStatus.UNREAD)
                .createdAt(LocalDateTime.now())
                .inAppSent(true)
                .actionUrl(actionUrl)
                .actionText(getActionTextByType(type))
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .senderName("System")
                .senderType("SYSTEM")
                .priority(getPriorityByType(type))
                .category(getCategoryByType(type))
                .metadata(createInstitutionMetadata(institution, relatedEntityId))
                .emailSent(true).smsSent(true).pushSent(true).build();

        return notificationRepository.save(notification);
    }

    // ============ ADMIN NOTIFICATION (GENERIC) ============
    @Override
    @Transactional
    public Notification createAdminNotification(String title, String message,
                                                NotificationType type, String actionUrl,
                                                String relatedEntityType, Long relatedEntityId) {

        log.info("Creating generic admin notification");

        Notification notification = Notification.builder()
                .userType(UserType.ADMIN)
                .title(title)
                .message(message)
                .type(type)
                .status(NotificationStatus.UNREAD)
                .createdAt(LocalDateTime.now())
                .inAppSent(true)
                .actionUrl(actionUrl)
                .actionText(getActionTextByType(type))
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .senderName("System")
                .senderType("SYSTEM")
                .priority(getPriorityByType(type))
                .category(getCategoryByType(type))
                .metadata(createAdminMetadata(null, relatedEntityId))
                .emailSent(true).smsSent(true).pushSent(true).build();

        return notificationRepository.save(notification);
    }

    // ============ ADMIN NOTIFICATION (SPECIFIC ADMIN) ============
    @Override
    @Transactional
    public Notification createAdminNotificationForSpecificAdmin(SystemAdmin admin, String title, String message,
                                                                NotificationType type, String actionUrl,
                                                                String relatedEntityType, Long relatedEntityId) {

        log.info("Creating admin notification for adminId: {}", admin.getAdminId());

        Notification notification = Notification.builder()
                .userType(UserType.ADMIN)
                .userId(admin.getAdminId())
                .admin(admin)
                .title(title)
                .message(message)
                .type(type)
                .status(NotificationStatus.UNREAD)
                .createdAt(LocalDateTime.now())
                .inAppSent(true)
                .actionUrl(actionUrl)
                .actionText(getActionTextByType(type))
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .senderName("System")
                .senderType("SYSTEM")
                .priority(getPriorityByType(type))
                .category(getCategoryByType(type))
                .metadata(createAdminMetadata(admin, relatedEntityId))
                .emailSent(true).smsSent(true).pushSent(true).build();

        return notificationRepository.save(notification);
    }

    // ============ SEND TO ALL ADMINS ============
    @Override
    @Transactional
    public void sendNotificationToAllAdmins(String title, String message,
                                            NotificationType type, String actionUrl,
                                            String relatedEntityType, Long relatedEntityId) {

        List<SystemAdmin> activeAdmins = adminRepository.findByActiveTrue();

        if (activeAdmins.isEmpty()) {
            log.warn("No active admins found to send notification");
            return;
        }

        log.info("Sending notification to {} active admins", activeAdmins.size());

        for (SystemAdmin admin : activeAdmins) {
            createAdminNotificationForSpecificAdmin(
                    admin, title, message, type, actionUrl, relatedEntityType, relatedEntityId
            );
        }
    }

    // ============= GENERIC METHODS =============
    @Override
    public Notification createNotification(Donor donor, String title, String message,
                                           NotificationType type, String actionUrl,
                                           String relatedEntityType, Long relatedEntityId,
                                           Institutions institution) {

        // Determine userType based on parameters
        UserType userType = UserType.DONOR; // Default
        Long userId = donor != null ? donor.getDonorId() : null;

        // If institution is provided without donor, it's for institution
        if (donor == null && institution != null) {
            userType = UserType.INSTITUTION;
            userId = institution.getInstitutionsId();
        }

        Notification notification = Notification.builder()
                // RECIPIENT INFORMATION (MUST SET)
                .userType(userType)
                .userId(userId)
                .donor(donor)
                .institution(institution)

                // NOTIFICATION CONTENT
                .title(title)
                .message(message)
                .type(type)

                // STATUS & METADATA
                .status(NotificationStatus.UNREAD)
                .createdAt(LocalDateTime.now())
                .inAppSent(true)

                // ACTION & NAVIGATION
                .actionUrl(actionUrl)
                .actionText(getActionTextByType(type))

                // RELATED ENTITY
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)

                // SENDER INFORMATION
                .senderName("System")
                .senderType("SYSTEM")

                // PRIORITY & CATEGORY
                .priority(getPriorityByType(type))
                .category(getCategoryByType(type))

                // ADDITIONAL DATA
                .metadata(createMetadata(donor, institution, relatedEntityId))

                .emailSent(true).smsSent(true).pushSent(true).build();

        return notificationRepository.save(notification);
    }
    private String getActionTextByType(NotificationType type) {
        switch (type) {
            case PAYMENT_REMINDER:
            case PAYMENT_CONFIRMED:
           // case PAYMENT_FAILED:
                return "View Payment";
            case SPONSORSHIP_CREATED:
            case SPONSORSHIP_EXPIRED:
                return "View Sponsorship";
            case INSTITUTION_APPROVED:
            case INSTITUTION_REJECTED:
                return "View Institution";
            case ADMIN_ALERT:
                return "Take Action";
            default:
                return "View Details";
        }
    }
    // Helper method to determine priority based on type
    private String getPriorityByType(NotificationType type) {
        switch (type) {
           // case PAYMENT_FAILED:
            case ADMIN_ALERT:
            case SECURITY_ALERT:
                return "HIGH";
            case PAYMENT_REMINDER:
            case INSTITUTION_REJECTED:
                return "MEDIUM";
            default:
                return "LOW";
        }
    }

    // Helper method to determine category based on type
    private String getCategoryByType(NotificationType type) {
        if (type.name().contains("PAYMENT")) {
            return "FINANCIAL";
        } else if (type.name().contains("SPONSORSHIP")) {
            return "SPONSORSHIP";
        } else if (type.name().contains("INSTITUTION")) {
            return "INSTITUTION";
        } else if (type.name().contains("DONOR")) {
            return "DONOR";
        } else if (type.name().contains("ADMIN")) {
            return "ADMIN";
        } else {
            return "SYSTEM";
        }
    }
    private String createDonorMetadata(Donor donor, Long relatedEntityId) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("donorId", donor.getDonorId());
        metadata.put("donorName", donor.getName());
        metadata.put("relatedEntityId", relatedEntityId);
        metadata.put("timestamp", LocalDateTime.now().toString());
        return convertToJson(metadata);
    }

    private String createInstitutionMetadata(Institutions institution, Long relatedEntityId) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("institutionId", institution.getInstitutionsId());
        metadata.put("institutionName", institution.getInstitutionName());
        metadata.put("relatedEntityId", relatedEntityId);
        metadata.put("timestamp", LocalDateTime.now().toString());
        return convertToJson(metadata);
    }

    private String createAdminMetadata(SystemAdmin admin, Long relatedEntityId) {
        Map<String, Object> metadata = new HashMap<>();
        if (admin != null) {
            metadata.put("adminId", admin.getAdminId());
            metadata.put("adminName", admin.getName());
        }
        metadata.put("relatedEntityId", relatedEntityId);
        metadata.put("timestamp", LocalDateTime.now().toString());
        return convertToJson(metadata);
    }

    private String convertToJson(Map<String, Object> data) {
        try {
            return new ObjectMapper().writeValueAsString(data);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
    // Helper method to create metadata JSON
    private String createMetadata(Donor donor, Institutions institution, Long relatedEntityId) {
        Map<String, Object> metadata = new HashMap<>();

        if (donor != null) {
            metadata.put("donorId", donor.getDonorId());
            metadata.put("donorName", donor.getName());
            metadata.put("donorEmail", donor.getEmail());
        }

        if (institution != null) {
            metadata.put("institutionId", institution.getInstitutionsId());
            metadata.put("institutionName", institution.getInstitutionName());
        }

        metadata.put("relatedEntityId", relatedEntityId);
        metadata.put("timestamp", LocalDateTime.now().toString());

        try {
            return new ObjectMapper().writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
    // ============= SPECIFIC NOTIFICATION METHODS =============
    @Override
    public void sendPaymentNotification(Donor donor, Sponsorship sponsorship) {
        createNotification(
                donor,
                "Payment Received",
                String.format("Your payment of ৳%.2f for %s has been received successfully.",
                        sponsorship.getMonthlyAmount(),
                        sponsorship.getStudent().getStudentName()),
                NotificationType.PAYMENT_RECEIVED,
                String.format("/donor/sponsorships/%d", sponsorship.getId()),
                "SPONSORSHIP",
                sponsorship.getId(),
                sponsorship.getStudent().getInstitution()
        );

        log.info("Payment notification sent to donor {}", donor.getEmail());
    }

//    @Override
//    public void sendInstitutionRegistrationNotification(Institutions institution) {
//        // Notification to institution
//        createNotification(
//                null, // no donor
//                "Registration Submitted",
//                String.format("Dear %s, your registration request has been submitted successfully. Our admin team will review it shortly.",
//                        institution.getInstitutionName()),
//                NotificationType.INSTITUTION_REGISTRATION,
//                "/institutions/dashboard",
//                "INSTITUTION",
//                institution.getInstitutionsId(),
//                institution
//        );
//
//        // Notification to all admins
//        sendAdminNotification(
//                "New Institution Registration",
//                String.format("Institution '%s' has submitted registration request. Please review.",
//                        institution.getInstitutionName()),
//                NotificationType.ADMIN_ALERT,
//                "INSTITUTION",
//                institution.getInstitutionsId()
//        );
//
//        log.info("Registration notification sent for institution {}", institution.getInstitutionName());
//    }
//@Override
//public void sendInstitutionRegistrationNotification(Institutions institution) {
//    try {
//        // Notification to institution
//        Notification institutionNotification = Notification.builder()
//                .userType(UserType.INSTITUTION)
//                .userId(institution.getInstitutionsId())
//                .institution(institution)
//                .title("Registration Submitted")
//                .message(String.format("Dear %s, your registration request has been submitted successfully. Our admin team will review it shortly.",
//                        institution.getInstitutionName()))
//                .type(NotificationType.INSTITUTION_REGISTRATION)
//                .status(NotificationStatus.UNREAD)
//                .createdAt(LocalDateTime.now())
//                .inAppSent(true)
//                .actionUrl("/institutions/dashboard")
//                .relatedEntityType("INSTITUTION")
//                .relatedEntityId(institution.getInstitutionsId())
//                .senderName("System")
//                .senderType("SYSTEM")
//                .emailSent(true).smsSent(true).pushSent(true).build();
//
//        notificationRepository.save(institutionNotification);
//
//        // Get all active admins
//        List<SystemAdmin> admins = adminRepository.findByActiveTrue();
//
//        for (SystemAdmin admin : admins) {
//            Notification adminNotification = Notification.builder()
//                    .userType(UserType.ADMIN)
//                    .userId(admin.getAdminId())
//                    .admin(admin)
//                    .title("New Institution Registration")
//                    .message(String.format("Institution '%s' has submitted registration request. Please review.",
//                            institution.getInstitutionName()))
//                    .type(NotificationType.ADMIN_ALERT)
//                    .status(NotificationStatus.UNREAD)
//                    .createdAt(LocalDateTime.now())
//                    .inAppSent(true)
//                    .actionUrl("/admin/institutions/pending")
//                    .relatedEntityType("INSTITUTION")
//                    .relatedEntityId(institution.getInstitutionsId())
//                    .senderName("System")
//                    .senderType("SYSTEM")
//                    .emailSent(true).smsSent(true).pushSent(true).build();
//
//            notificationRepository.save(adminNotification);
//        }
//
//        log.info("Registration notification sent for institution {}", institution.getInstitutionName());
//
//    } catch (Exception e) {
//        log.error("Failed to send notification for institution {}: {}",
//                institution.getInstitutionName(), e.getMessage());
//        // Don't throw exception - notification failure shouldn't break registration
//    }
//}

    @Override
    public void sendInstitutionApprovedNotification(Institutions institution, SystemAdmin approvedBy) {
        createNotification(
                null,
                "Account Approved!",
                String.format("Congratulations %s! Your institution has been approved by Admin %s. You can now login to your account.",
                        institution.getInstitutionName(), approvedBy.getName()),
                NotificationType.INSTITUTION_APPROVED,
                "/login",
                "INSTITUTION",
                institution.getInstitutionsId(),
                institution
        );

        log.info("Approval notification sent to institution {}", institution.getInstitutionName());
    }

    @Override
    public void sendInstitutionRejectedNotification(Institutions institution, SystemAdmin rejectedBy, String reason) {
        createNotification(
                null,
                "Registration Rejected",
                String.format("Dear %s, your registration request has been rejected by Admin %s. Reason: %s",
                        institution.getInstitutionName(), rejectedBy.getName(), reason),
                NotificationType.INSTITUTION_REJECTED,
                "/contact-support",
                "INSTITUTION",
                institution.getInstitutionsId(),
                institution
        );

        log.info("Rejection notification sent to institution {}", institution.getInstitutionName());
    }

    @Override
    public void sendSponsorshipNotificationToInstitution(Institutions institution, Donor donor, Long childId) {
        // To institution
        createNotification(
                null,
                "New Sponsorship Request",
                String.format("Donor %s wants to sponsor a student in your institution (Student ID: %d). Please review and confirm.",
                        donor.getName(), childId),
                NotificationType.SPONSORSHIP_CREATED,
                String.format("/institutions/children/%d", childId),
                "SPONSORSHIP",
                childId,
                institution
        );

        // To donor
        createNotification(
                donor,
                "Sponsorship Request Sent",
                String.format("Your sponsorship request has been sent to %s. They will review and confirm shortly.",
                        institution.getInstitutionName()),
                NotificationType.SPONSORSHIP_CREATED,
                String.format("/donor/sponsorships/%d", childId),
                "SPONSORSHIP",
                childId,
                institution
        );

        log.info("Sponsorship notification sent to institution {} and donor {}",
                institution.getInstitutionName(), donor.getEmail());
    }

    @Override
    public void sendAdminNotification(String title, String message, NotificationType type,
                                      String relatedEntityType, Long relatedEntityId) {
        createNotification(
                null, // no donor
                title,
                message,
                type,
                "/admin/dashboard",
                relatedEntityType,
                relatedEntityId,
                null // no institution
        );
    }

    // ============= USER TYPE BASED METHODS =============
    @Override
    public List<Notification> getNotificationsByUserType(String userType, Long userId) {
        if ("DONOR".equalsIgnoreCase(userType)) {
            return getDonorNotifications(userId);
        } else if ("INSTITUTION".equalsIgnoreCase(userType)) {
            return getInstitutionNotifications(userId);
        } else if ("ADMIN".equalsIgnoreCase(userType)) {
            // For admin, show all notifications or specific admin notifications
            return notificationRepository.findByRelatedEntityTypeOrderByCreatedAtDesc("ADMIN");
        }
        throw new IllegalArgumentException("Invalid user type: " + userType);
    }

    @Override
    public Long getUnreadCountByUserType(String userType, Long userId) {
        if ("DONOR".equalsIgnoreCase(userType)) {
            return getUnreadCount(userId);
        } else if ("INSTITUTION".equalsIgnoreCase(userType)) {
            return getUnreadCountForInstitution(userId);
        } else if ("ADMIN".equalsIgnoreCase(userType)) {
            return notificationRepository.countByStatus(NotificationStatus.UNREAD);
        }
        throw new IllegalArgumentException("Invalid user type: " + userType);
    }





    // Admin notifications methods
    public List<Notification> getAdminNotifications(Long adminId) {
        if (adminId != null) {
            // Get notifications for specific admin
            return notificationRepository.findByUserTypeAndUserIdOrderByCreatedAtDesc(
                    UserType.ADMIN, adminId);
        } else {
            // Get all admin notifications (for super admin)
            return notificationRepository.findByUserTypeOrderByCreatedAtDesc(UserType.ADMIN);
        }
    }


}
