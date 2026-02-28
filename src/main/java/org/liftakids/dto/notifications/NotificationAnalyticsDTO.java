package org.liftakids.dto.notifications;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationAnalyticsDTO {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private long totalNotifications;
    private long emailSent;
    private long smsSent;
    private long pushSent;
    private double readRate;
    private Double avgResponseTimeHours;
    private List<Object[]> hourlyDistribution;
    private List<Object[]> topNotificationTypes;
}
