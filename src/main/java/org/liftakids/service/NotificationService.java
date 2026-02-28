package org.liftakids.service;

import org.liftakids.entity.*;
import org.liftakids.entity.enm.NotificationType;

import java.util.List;

public interface NotificationService {
    // Existing donor methods
    List<Notification> getDonorNotifications(Long donorId);
    List<Notification> getUnreadNotifications(Long donorId);
    Long getUnreadCount(Long donorId);
    void markAsRead(Long notificationId, Long donorId);
    void markAllAsRead(Long donorId);

    // New institution methods
    List<Notification> getInstitutionNotifications(Long institutionId);
    List<Notification> getUnreadNotificationsForInstitution(Long institutionId);
    Long getUnreadCountForInstitution(Long institutionId);
    void markAsReadForInstitution(Long notificationId, Long institutionId);
    void markAllAsReadForInstitution(Long institutionId);
    // Separate methods for different user types
    Notification createDonorNotification(Donor donor, String title, String message,
                                         NotificationType type, String actionUrl,
                                         String relatedEntityType, Long relatedEntityId);

    Notification createInstitutionNotification(Institutions institution, String title, String message,
                                               NotificationType type, String actionUrl,
                                               String relatedEntityType, Long relatedEntityId);

    Notification createAdminNotification(String title, String message,
                                         NotificationType type, String actionUrl,
                                         String relatedEntityType, Long relatedEntityId);

    Notification createAdminNotificationForSpecificAdmin(SystemAdmin admin, String title, String message,
                                                         NotificationType type, String actionUrl,
                                                         String relatedEntityType, Long relatedEntityId);
    // Bulk notifications
    void sendNotificationToAllAdmins(String title, String message,
                                     NotificationType type, String actionUrl,
                                     String relatedEntityType, Long relatedEntityId);
    // Generic create notification
    Notification createNotification(Donor donor, String title, String message,
                                    NotificationType type, String actionUrl,
                                    String relatedEntityType, Long relatedEntityId,
                                    Institutions institution);

    // Specific notification methods
    void sendPaymentNotification(Donor donor, Sponsorship sponsorship);
    void sendInstitutionRegistrationNotification(Institutions institution);
    void sendInstitutionApprovedNotification(Institutions institution, SystemAdmin approvedBy);
    void sendInstitutionRejectedNotification(Institutions institution, SystemAdmin rejectedBy, String reason);
    void sendSponsorshipNotificationToInstitution(Institutions institution, Donor donor, Long childId);

    // Admin notification methods
    void sendAdminNotification(String title, String message, NotificationType type,
                               String relatedEntityType, Long relatedEntityId);

    // Get notifications by user type
    List<Notification> getNotificationsByUserType(String userType, Long userId);
    Long getUnreadCountByUserType(String userType, Long userId);

}
