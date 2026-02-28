package org.liftakids.service;

import org.liftakids.dto.notifications.NotificationAnalyticsDTO;
import org.liftakids.dto.notifications.NotificationFilterDTO;
import org.liftakids.dto.notifications.NotificationResponseDTO;
import org.liftakids.entity.Notification;
import org.liftakids.entity.enm.NotificationStatus;
import org.liftakids.entity.enm.NotificationType;
import org.liftakids.entity.enm.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AdminNotificationService {
    // Get all notifications (system-wide) with pagination
    Page<NotificationResponseDTO> getAllNotificationsForAdmin(Pageable pageable);

    // Get filtered notifications for admin
    Page<Notification> getFilteredNotificationsForAdminView(
            NotificationFilterDTO filter,
            Pageable pageable);

    // Get notifications by specific criteria
    List<Notification> getNotificationsByCriteria(
            UserType userType,
            NotificationType notificationType,
            NotificationStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    void sendNotificationToUserType(
            UserType userType,
            String title,
            String message,
            String actionUrl,
            Long adminId);
    void sendSystemAnnouncement(
            String title,
            String message,
            String actionUrl,
            Long adminId);


    // Get notification statistics
    Map<String, Object> getNotificationStatistics();

    // Bulk operations
    int bulkMarkAsRead(List<Long> notificationIds, Long adminId);
    int bulkDeleteNotifications(List<Long> notificationIds, Long adminId);

    // Send system announcement (to all users)


    // Export notifications to Excel/CSV
   // byte[] exportNotifications(NotificationFilterDTO filter);

    // Get notification analytics
    NotificationAnalyticsDTO getNotificationAnalytics(
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    // Clear old notifications (older than X days)
    int clearOldNotifications(int days, Long adminId);

    // Resend failed notifications
    List<Notification> resendFailedNotifications(List<Long> notificationIds);


    Notification createAdminNotificationForSpecificAdmin(
            Long adminId,
            String title,
            String message,
            NotificationType type,
            String actionUrl,
            String relatedEntityType,
            Long relatedEntityId
    );

    Notification createInstitutionNotificationForAdmin(
            Long institutionId,
            String title,
            String message,
            NotificationType type,
            String actionUrl,
            String relatedEntityType,
            Long relatedEntityId,
            Long adminId
    );

    Notification createDonorNotificationForAdmin(
            Long donorId,
            String title,
            String message,
            NotificationType type,
            String actionUrl,
            String relatedEntityType,
            Long relatedEntityId,
            Long adminId
    );

    public Long getAdminUnreadCount(Long adminId);



}

