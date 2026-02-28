package org.liftakids.controller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.liftakids.dto.notifications.*;
import org.liftakids.entity.Notification;
import org.liftakids.entity.enm.NotificationStatus;
import org.liftakids.entity.enm.NotificationType;
import org.liftakids.entity.enm.UserType;
import org.liftakids.service.AdminNotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
@Slf4j
public class AdminNotificationController {

    private final AdminNotificationService adminNotificationService;

    // ============= GET ALL NOTIFICATIONS (PAGINATED) =============
    @GetMapping("/all")
    public ResponseEntity<Page<NotificationResponseDTO>> getAllNotifications(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.info("Admin fetching all notifications, page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<NotificationResponseDTO> notifications = adminNotificationService.getAllNotificationsForAdmin(pageable);
        return ResponseEntity.ok(notifications);
    }

    // ============= FILTER NOTIFICATIONS =============
    @PostMapping("/filter")
    public ResponseEntity<Page<Notification>> filterNotifications(
            @RequestBody NotificationFilterDTO filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.info("Admin filtering notifications with criteria: {}", filter);

        Page<Notification> notifications = adminNotificationService.getFilteredNotificationsForAdminView(filter, pageable);
        return ResponseEntity.ok(notifications);
    }

    // ============= GET NOTIFICATIONS BY CRITERIA =============
    @GetMapping("/by-criteria")
    public ResponseEntity<List<Notification>> getNotificationsByCriteria(
            @RequestParam(required = false) UserType userType,
            @RequestParam(required = false) NotificationType notificationType,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate) {

        log.info("Admin fetching notifications by criteria - userType: {}, notificationType: {}, status: {}",
                userType, notificationType, status);

        List<Notification> notifications = adminNotificationService.getNotificationsByCriteria(
                userType, notificationType, status, startDate, endDate);
        return ResponseEntity.ok(notifications);
    }

    // ============= GET NOTIFICATION STATISTICS =============
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getNotificationStatistics() {

        log.info("Admin fetching notification statistics");

        Map<String, Object> statistics = adminNotificationService.getNotificationStatistics();
        return ResponseEntity.ok(statistics);
    }

    // ============= GET NOTIFICATION ANALYTICS =============
    @GetMapping("/analytics")
    public ResponseEntity<NotificationAnalyticsDTO> getNotificationAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate) {

        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        log.info("Admin fetching notification analytics from {} to {}", startDate, endDate);

        NotificationAnalyticsDTO analytics = adminNotificationService.getNotificationAnalytics(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    // ============= BULK MARK AS READ =============
    @PutMapping("/bulk/mark-read")
    public ResponseEntity<Map<String, Object>> bulkMarkAsRead(
            @RequestBody BulkOperationDTO request) {

        log.info("Admin {} bulk marking {} notifications as read",
                request.getAdminId(), request.getNotificationIds().size());

        int markedCount = adminNotificationService.bulkMarkAsRead(
                request.getNotificationIds(), request.getAdminId());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Notifications marked as read successfully");
        response.put("markedCount", markedCount);
        response.put("adminId", request.getAdminId());

        return ResponseEntity.ok(response);
    }

    // ============= BULK DELETE NOTIFICATIONS =============
    @DeleteMapping("/bulk/delete")
    public ResponseEntity<Map<String, Object>> bulkDeleteNotifications(
            @RequestBody BulkOperationDTO request) {

        log.info("Admin {} bulk deleting {} notifications",
                request.getAdminId(), request.getNotificationIds().size());

        int deletedCount = adminNotificationService.bulkDeleteNotifications(
                request.getNotificationIds(), request.getAdminId());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Notifications deleted successfully");
        response.put("deletedCount", deletedCount);
        response.put("adminId", request.getAdminId());

        return ResponseEntity.ok(response);
    }

    // ============= SEND SYSTEM ANNOUNCEMENT =============
    @PostMapping("/announcement")
    public ResponseEntity<Map<String, Object>> sendSystemAnnouncement(
            @RequestBody SystemAnnouncementDTO request) {

        log.info("Admin {} sending system announcement: {}",
                request.getAdminId(), request.getTitle());

        adminNotificationService.sendSystemAnnouncement(
                request.getTitle(),
                request.getMessage(),
                request.getActionUrl(),
                request.getAdminId()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("message", "System announcement sent successfully");
        response.put("title", request.getTitle());
        response.put("adminId", request.getAdminId());
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ============= SEND NOTIFICATION TO USER TYPE =============
    @PostMapping("/send-to-usertype")
    public ResponseEntity<Map<String, Object>> sendNotificationToUserType(
            @RequestBody UserTypeNotificationDTO request) {

        log.info("Admin {} sending notification to all {} users",
                request.getAdminId(), request.getUserType());

        UserType userType = UserType.valueOf(request.getUserType().toUpperCase());

        adminNotificationService.sendNotificationToUserType(
                userType,
                request.getTitle(),
                request.getMessage(),
                request.getActionUrl(),
                request.getAdminId()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("message", String.format("Notification sent to all %s users", request.getUserType()));
        response.put("title", request.getTitle());
        response.put("userType", request.getUserType());
        response.put("adminId", request.getAdminId());
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ============= CLEAR OLD NOTIFICATIONS =============
    @DeleteMapping("/clear-old")
    public ResponseEntity<Map<String, Object>> clearOldNotifications(
            @RequestParam int days,
            @RequestParam Long adminId) {

        log.info("Admin {} clearing notifications older than {} days", adminId, days);

        int clearedCount = adminNotificationService.clearOldNotifications(days, adminId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Old notifications cleared successfully");
        response.put("clearedCount", clearedCount);
        response.put("days", days);
        response.put("adminId", adminId);
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    // ============= RESEND FAILED NOTIFICATIONS =============
    @PostMapping("/resend-failed")
    public ResponseEntity<Map<String, Object>> resendFailedNotifications(
            @RequestBody List<Long> notificationIds) {

        log.info("Attempting to resend {} failed notifications", notificationIds.size());

        List<Notification> resentNotifications = adminNotificationService.resendFailedNotifications(notificationIds);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Failed notifications reset for resending");
        response.put("resentCount", resentNotifications.size());
        response.put("notificationIds", notificationIds);
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    // ============= CREATE NOTIFICATION FOR SPECIFIC ADMIN =============
    @PostMapping("/create/admin")
    public ResponseEntity<Notification> createAdminNotification(
            @RequestBody CreateAdminNotificationDTO request) {

        log.info("Creating admin notification for adminId: {}", request.getAdminId());

        Notification notification = adminNotificationService.createAdminNotificationForSpecificAdmin(
                request.getAdminId(),
                request.getTitle(),
                request.getMessage(),
                request.getType(),
                request.getActionUrl(),
                request.getRelatedEntityType(),
                request.getRelatedEntityId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }

    // ============= CREATE NOTIFICATION FOR INSTITUTION =============
    @PostMapping("/create/institution")
    public ResponseEntity<Notification> createInstitutionNotification(
            @RequestBody CreateInstitutionNotificationDTO request) {

        log.info("Creating institution notification for institutionId: {} by adminId: {}",
                request.getInstitutionId(), request.getAdminId());

        Notification notification = adminNotificationService.createInstitutionNotificationForAdmin(
                request.getInstitutionId(),
                request.getTitle(),
                request.getMessage(),
                request.getType(),
                request.getActionUrl(),
                request.getRelatedEntityType(),
                request.getRelatedEntityId(),
                request.getAdminId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }

    // ============= CREATE NOTIFICATION FOR DONOR =============
    @PostMapping("/create/donor")
    public ResponseEntity<Notification> createDonorNotification(
            @RequestBody CreateDonorNotificationDTO request) {

        log.info("Creating donor notification for donorId: {} by adminId: {}",
                request.getDonorId(), request.getAdminId());

        Notification notification = adminNotificationService.createDonorNotificationForAdmin(
                request.getDonorId(),
                request.getTitle(),
                request.getMessage(),
                request.getType(),
                request.getActionUrl(),
                request.getRelatedEntityType(),
                request.getRelatedEntityId(),
                request.getAdminId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }

    // ============= GET DASHBOARD SUMMARY =============
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {

        log.info("Admin fetching notification dashboard summary");

        Map<String, Object> dashboard = new HashMap<>();

        // Get statistics
        Map<String, Object> statistics = adminNotificationService.getNotificationStatistics();
        dashboard.put("statistics", statistics);

        // Get recent notifications
        Pageable recentPageable = Pageable.ofSize(10);
        Page<NotificationResponseDTO> recentNotifications = adminNotificationService.getAllNotificationsForAdmin(recentPageable);
        dashboard.put("recentNotifications", recentNotifications.getContent());

        // Get analytics for last 7 days
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        NotificationAnalyticsDTO weeklyAnalytics = adminNotificationService.getNotificationAnalytics(
                weekAgo, LocalDateTime.now());
        dashboard.put("weeklyAnalytics", weeklyAnalytics);

        return ResponseEntity.ok(dashboard);
    }

    // ============= GET NOTIFICATION BY ID =============
    @GetMapping("/{notificationId}")
    public ResponseEntity<Notification> getNotificationById(@PathVariable Long notificationId) {

        log.info("Admin fetching notification by ID: {}", notificationId);

        // You need to add this method to your service
        // Notification notification = adminNotificationService.getNotificationById(notificationId);

        // For now, use the repository directly or implement the method
        return ResponseEntity.notFound().build();
    }

    // ============= DELETE SINGLE NOTIFICATION =============
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Map<String, Object>> deleteNotification(
            @PathVariable Long notificationId,
            @RequestParam Long adminId) {

        log.info("Admin {} deleting notification ID: {}", adminId, notificationId);

        // Create a bulk operation with single ID
        BulkOperationDTO request = new BulkOperationDTO();
        request.setNotificationIds(List.of(notificationId));
        request.setAdminId(adminId);

        int deletedCount = adminNotificationService.bulkDeleteNotifications(
                request.getNotificationIds(), request.getAdminId());

        Map<String, Object> response = new HashMap<>();
        if (deletedCount > 0) {
            response.put("message", "Notification deleted successfully");
            response.put("notificationId", notificationId);
            response.put("adminId", adminId);
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Notification not found or could not be deleted");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // ============= MARK SINGLE NOTIFICATION AS READ =============
    @PutMapping("/{notificationId}/mark-read")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable Long notificationId,
            @RequestParam Long adminId) {

        log.info("Admin {} marking notification {} as read", adminId, notificationId);

        // Create a bulk operation with single ID
        BulkOperationDTO request = new BulkOperationDTO();
        request.setNotificationIds(List.of(notificationId));
        request.setAdminId(adminId);

        int markedCount = adminNotificationService.bulkMarkAsRead(
                request.getNotificationIds(), request.getAdminId());

        Map<String, Object> response = new HashMap<>();
        if (markedCount > 0) {
            response.put("message", "Notification marked as read successfully");
            response.put("notificationId", notificationId);
            response.put("adminId", adminId);
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Notification not found or could not be marked as read");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCountForAdmin(
            @RequestParam Long adminId) {

        log.info("Getting unread count for admin: {}", adminId);

        long unreadCount = adminNotificationService.getAdminUnreadCount(adminId);

        Map<String, Object> response = new HashMap<>();
        response.put("unreadCount", unreadCount);
        response.put("adminId", adminId);
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

}
