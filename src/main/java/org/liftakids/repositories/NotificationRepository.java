package org.liftakids.repositories;

import jakarta.transaction.Transactional;
import org.liftakids.entity.Notification;
import org.liftakids.entity.enm.NotificationStatus;
import org.liftakids.entity.enm.NotificationType;
import org.liftakids.entity.enm.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // ============= DONOR QUERIES =============
    List<Notification> findByDonorDonorIdOrderByCreatedAtDesc(Long donorId);
    List<Notification> findByDonorDonorIdAndStatusOrderByCreatedAtDesc(Long donorId, NotificationStatus status);
    Long countByDonorDonorIdAndStatus(Long donorId, NotificationStatus status);
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    int deleteOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
    @Modifying
    @Query("UPDATE Notification n SET n.status = :status, n.readAt = :readAt " +
            "WHERE n.notificationId = :notificationId AND n.donor.donorId = :donorId")
    int markAsReadForDonor(@Param("notificationId") Long notificationId,
                           @Param("donorId") Long donorId,
                           @Param("status") NotificationStatus status,
                           @Param("readAt") LocalDateTime readAt);

    @Modifying
    @Query("UPDATE Notification n SET n.status = :status, n.readAt = :readAt " +
            "WHERE n.donor.donorId = :donorId AND n.status = 'UNREAD'")
    int markAllAsReadForDonor(@Param("donorId") Long donorId,
                              @Param("status") NotificationStatus status,
                              @Param("readAt") LocalDateTime readAt);

    // ============= INSTITUTION QUERIES =============
    List<Notification> findByInstitutionInstitutionsIdOrderByCreatedAtDesc(Long institutionsId);
    List<Notification> findByInstitutionInstitutionsIdAndStatusOrderByCreatedAtDesc(Long institutionsId, NotificationStatus status);
    Long countByInstitutionInstitutionsIdAndStatus(Long institutionsId, NotificationStatus status);

    @Modifying
    @Query("UPDATE Notification n SET n.status = :status, n.readAt = :readAt " +
            "WHERE n.notificationId = :notificationId AND n.institution.institutionsId = :institutionsId")
    int markAsReadForInstitution(@Param("notificationId") Long notificationId,
                                 @Param("institutionsId") Long institutionsId,
                                 @Param("status") NotificationStatus status,
                                 @Param("readAt") LocalDateTime readAt);

    @Modifying
    @Query("UPDATE Notification n SET n.status = :status, n.readAt = :readAt " +
            "WHERE n.institution.institutionsId = :institutionId AND n.status = 'UNREAD'")
    int markAllAsReadForInstitution(@Param("institutionId") Long institutionsId,
                                    @Param("status") NotificationStatus status,
                                    @Param("readAt") LocalDateTime readAt);

    // ============= GENERAL QUERIES =============
    List<Notification> findByRelatedEntityTypeOrderByCreatedAtDesc(String entityType);
    Long countByStatus(NotificationStatus status);

    // Find notifications by related entity
    List<Notification> findByRelatedEntityTypeAndRelatedEntityId(String entityType, Long entityId);

    // Find notifications without donor or institution (admin notifications)
    List<Notification> findByDonorIsNullAndInstitutionIsNullOrderByCreatedAtDesc();

    // Find by userType and userId (NEW)
    List<Notification> findByUserTypeAndUserIdOrderByCreatedAtDesc(UserType userType, Long userId);

    // Find by userType and userId with status (NEW)
    List<Notification> findByUserTypeAndUserIdAndStatusOrderByCreatedAtDesc(
            UserType userType, Long userId, NotificationStatus status);

    // Count by userType, userId and status (NEW)
    Long countByUserTypeAndUserIdAndStatus(UserType userType, Long userId, NotificationStatus status);
   // List<Notification> getNotificationsByUserType(String userType, Long userId);

    // ADD THIS METHOD:
   // Long getUnreadCountByUserType(String userType, Long userId);


    // Find by userType (for admin)
    List<Notification> findByUserTypeOrderByCreatedAtDesc(UserType userType);

    // Count by userType and status (for admin)
    Long countByUserTypeAndStatus(UserType userType, NotificationStatus status);

    Page<Notification> findByUserTypeOrderByCreatedAtDesc(UserType userType, Pageable pageable);
    Page<Notification> findByStatusOrderByCreatedAtDesc(NotificationStatus status, Pageable pageable);

    Page<Notification> findByUserTypeAndCreatedAtBetweenOrderByCreatedAtDesc(
            UserType userType, LocalDateTime start, LocalDateTime end, Pageable pageable);

    // admin

    // Get all with pagination
    Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Count by user type
    long countByUserType(UserType userType);

    // Count by notification type
    @Query("SELECT n.type, COUNT(n) FROM Notification n GROUP BY n.type ORDER BY COUNT(n) DESC")
    List<Object[]> countByNotificationType();

    // Daily counts
    // Get daily counts
    @Query(value = "SELECT DATE(n.created_at) as date, COUNT(*) as count FROM notifications n " +
            "WHERE n.created_at >= ?1 GROUP BY DATE(n.created_at) ORDER BY date DESC",
            nativeQuery = true)
    List<Object[]> getDailyCounts(int startDate);

    // Get monthly counts
    @Query(value = "SELECT DATE_FORMAT(n.created_at, '%Y-%m') as month, COUNT(*) as count " +
            "FROM notifications n WHERE n.created_at >= ?1 " +
            "GROUP BY DATE_FORMAT(n.created_at, '%Y-%m') ORDER BY month DESC",
            nativeQuery = true)
    List<Object[]> getMonthlyCounts(int startDate);

    // Average response time (time between creation and read)
    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, n.createdAt, n.readAt)) FROM Notification n " +
            "WHERE n.readAt IS NOT NULL")
    Double getAverageResponseTime();
    Page<Notification> findByTypeOrderByCreatedAtDesc(NotificationType type, Pageable pageable);
    // Bulk update status
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.status = :status, n.readAt = :readAt " +
            "WHERE n.notificationId IN :notificationIds")
    int bulkUpdateStatus(@Param("notificationIds") List<Long> notificationIds,
                         @Param("status") NotificationStatus status,
                         @Param("readAt") LocalDateTime readAt);

    // Delete by IDs
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.notificationId IN :notificationIds")
    int deleteByIdIn(@Param("notificationIds") List<Long> notificationIds);

    // Find by criteria (custom query)
    @Query("SELECT n FROM Notification n WHERE " +
            "(:userType IS NULL OR n.userType = :userType) AND " +
            "(:notificationType IS NULL OR n.type = :notificationType) AND " +
            "(:status IS NULL OR n.status = :status) AND " +
            "(:startDate IS NULL OR n.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR n.createdAt <= :endDate) " +
            "ORDER BY n.createdAt DESC")
    List<Notification> findByCriteria(
            @Param("userType") UserType userType,
            @Param("notificationType") NotificationType notificationType,
            @Param("status") NotificationStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    long countByStatusAndCreatedAtBetween(
            NotificationStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
    // Count between dates
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Count by email sent and date range
    long countByEmailSentAndCreatedAtBetween(boolean emailSent, LocalDateTime start, LocalDateTime end);

    // Count by SMS sent and date range
    long countBySmsSentAndCreatedAtBetween(boolean smsSent, LocalDateTime start, LocalDateTime end);

    // Count by push sent and date range
    long countByPushSentAndCreatedAtBetween(boolean pushSent, LocalDateTime start, LocalDateTime end);

    // Find notifications older than date
    List<Notification> findByCreatedAtBefore(LocalDateTime date);

    // Find by IDs
   // List<Notification> findByIdIn(List<Long> ids);

    // Hourly distribution
    @Query("SELECT HOUR(n.createdAt), COUNT(n) FROM Notification n " +
            "WHERE n.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY HOUR(n.createdAt) " +
            "ORDER BY HOUR(n.createdAt)")
    List<Object[]> getHourlyDistribution(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);
    List<Notification> findByUserTypeAndTypeAndStatusAndCreatedAtBetweenOrderByCreatedAtDesc(
            UserType userType, NotificationType type, NotificationStatus status,
            LocalDateTime start, LocalDateTime end);

    Page<Notification> findByUserTypeAndStatusOrderByCreatedAtDesc(
            UserType userType, NotificationStatus status, Pageable pageable);

    Page<Notification> findByUserTypeAndTypeAndStatusOrderByCreatedAtDesc(
            UserType userType, NotificationType type, NotificationStatus status, Pageable pageable);

    Page<Notification> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime start, LocalDateTime end, Pageable pageable);

    // Top notification types
    @Query("SELECT n.type, COUNT(n) FROM Notification n " +
            "WHERE n.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY n.type " +
            "ORDER BY COUNT(n) DESC")
    List<Object[]> getTopNotificationTypes(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate,
                                           @Param("limit") int limit);

    // Average response time between dates
    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, n.createdAt, n.readAt)) FROM Notification n " +
            "WHERE n.readAt IS NOT NULL AND n.createdAt BETWEEN :startDate AND :endDate")
    Double getAverageResponseTimeBetween(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

}
