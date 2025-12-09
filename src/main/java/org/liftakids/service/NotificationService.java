package org.liftakids.service;

import org.liftakids.entity.Donor;
import org.liftakids.entity.Institutions;
import org.liftakids.entity.Notification;
import org.liftakids.entity.Sponsorship;
import org.liftakids.entity.enm.NotificationType;

import java.util.List;

public interface NotificationService {
    List<Notification> getDonorNotifications(Long donorId);
    List<Notification> getUnreadNotifications(Long donorId);
    Long getUnreadCount(Long donorId);
    void markAsRead(Long notificationId, Long donorId);
    void markAllAsRead(Long donorId);
    Notification createNotification(Donor donor, String title, String message,
                                    NotificationType type, String actionUrl,
                                    String relatedEntityType, Long relatedEntityId,
                                    Institutions institution);
    void sendPaymentNotification(Donor donor, Sponsorship sponsorship);
}
