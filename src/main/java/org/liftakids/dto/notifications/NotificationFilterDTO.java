package org.liftakids.dto.notifications;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.liftakids.entity.enm.NotificationStatus;
import org.liftakids.entity.enm.NotificationType;
import org.liftakids.entity.enm.UserType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationFilterDTO {
    private UserType userType;
    private NotificationType notificationType;
    private NotificationStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String searchTerm;
    private Integer days;
}
