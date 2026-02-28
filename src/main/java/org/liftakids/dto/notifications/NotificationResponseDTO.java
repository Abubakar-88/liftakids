package org.liftakids.dto.notifications;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.liftakids.entity.Notification;
import org.liftakids.entity.enm.NotificationStatus;
import org.liftakids.entity.enm.NotificationType;
import org.liftakids.entity.enm.UserType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDTO {
    private Long notificationId;
    private String title;
    private String message;
    private NotificationType type;
    private NotificationStatus status;
    private UserType userType;
    private Long userId;
    private String userName;
    private String relatedEntityType;
    private Long relatedEntityId;
    private String relatedEntityName;
    private String actionUrl;
    private String actionText;
    private String icon;
    private String priority;
    private String category;
    private String senderName;
    private String senderType;
    private Long senderId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readAt;

    // Constructor from Notification entity
    public NotificationResponseDTO(Notification notification) {
        this.notificationId = notification.getNotificationId();
        this.title = notification.getTitle();
        this.message = notification.getMessage();
        this.type = notification.getType();
        this.status = notification.getStatus();
        this.userType = notification.getUserType();
        this.userId = notification.getUserId();
        this.userName = extractUserName(notification);
        this.relatedEntityType = notification.getRelatedEntityType();
        this.relatedEntityId = notification.getRelatedEntityId();
        this.relatedEntityName = notification.getRelatedEntityName();
        this.actionUrl = notification.getActionUrl();
        this.actionText = notification.getActionText();
        this.icon = notification.getIcon();
        this.priority = notification.getPriority();
        this.category = notification.getCategory();
        this.senderName = notification.getSenderName();
        this.senderType = notification.getSenderType();
        this.senderId = notification.getSenderId();
        this.createdAt = notification.getCreatedAt();
        this.readAt = notification.getReadAt();
    }

    private String extractUserName(Notification notification) {
        // Extract user name based on relationships
        if (notification.getDonor() != null) {
            return notification.getDonor().getName();
        } else if (notification.getInstitution() != null) {
            return notification.getInstitution().getInstitutionName();
        } else if (notification.getAdmin() != null) {
            return notification.getAdmin().getName();
        }
        return null;
    }
}