package org.liftakids.repositories;

import org.liftakids.entity.Notification;
import org.liftakids.entity.enm.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByDonorDonorIdOrderByCreatedAtDesc(Long donorId);

    List<Notification> findByDonorDonorIdAndStatusOrderByCreatedAtDesc(
            Long donorId, NotificationStatus status);

    Long countByDonorDonorIdAndStatus(Long donorId, NotificationStatus status);

    @Modifying
    @Query("UPDATE Notification n SET n.status = :status, n.readAt = :readAt " +
            "WHERE n.notificationId = :notificationId AND n.donor.donorId = :donorId")
    int markAsRead(@Param("notificationId") Long notificationId,
                   @Param("donorId") Long donorId,
                   @Param("status") NotificationStatus status,
                   @Param("readAt") LocalDateTime readAt);

    @Modifying
    @Query("UPDATE Notification n SET n.status = :status, n.readAt = :readAt " +
            "WHERE n.donor.donorId = :donorId AND n.status = 'UNREAD'")
    int markAllAsRead(@Param("donorId") Long donorId,
                      @Param("status") NotificationStatus status,
                      @Param("readAt") LocalDateTime readAt);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    int deleteOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

    List<Notification> findByRelatedEntityTypeAndRelatedEntityId(
            String relatedEntityType, Long relatedEntityId);
}
