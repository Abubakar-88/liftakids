package org.liftakids.controller;

import lombok.RequiredArgsConstructor;
import org.liftakids.entity.Notification;
import org.liftakids.entity.enm.UserType;
import org.liftakids.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    // ============= COMMON ENDPOINTS (User Type based) =============
    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(
            @RequestParam String userType,
            @RequestParam(required = false) Long userId) {

        try {
            UserType type = UserType.valueOf(userType.toUpperCase());

            if (userId == null && type != UserType.ADMIN) {
                return ResponseEntity.badRequest().build();
            }

            List<Notification> notifications;

            switch (type) {
                case DONOR:
                    notifications = notificationService.getDonorNotifications(userId);
                    break;
                case INSTITUTION:
                    notifications = notificationService.getInstitutionNotifications(userId);
                    break;

                default:
                    return ResponseEntity.badRequest().build();
            }

            return ResponseEntity.ok(notifications);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(
            @RequestParam String userType,
            @RequestParam Long userId) {

        List<Notification> notifications;

        if ("DONOR".equalsIgnoreCase(userType)) {
            notifications = notificationService.getUnreadNotifications(userId);
        } else if ("INSTITUTION".equalsIgnoreCase(userType)) {
            notifications = notificationService.getUnreadNotificationsForInstitution(userId);
        }  else {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @RequestParam String userType,
            @RequestParam(required = false) Long userId) {

        try {
            UserType type = UserType.valueOf(userType.toUpperCase());
            Long count = 0L;

            switch (type) {
                case DONOR:
                    if (userId == null) return ResponseEntity.badRequest().build();
                    count = notificationService.getUnreadCount(userId);
                    break;
                case INSTITUTION:
                    if (userId == null) return ResponseEntity.badRequest().build();
                    count = notificationService.getUnreadCountForInstitution(userId);
                    break;

                default:
                    return ResponseEntity.badRequest().build();
            }

            Map<String, Long> response = new HashMap<>();
            response.put("unreadCount", count);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long notificationId,
            @RequestParam String userType,
            @RequestParam Long userId) {

        if ("DONOR".equalsIgnoreCase(userType)) {
            notificationService.markAsRead(notificationId, userId);
        } else if ("INSTITUTION".equalsIgnoreCase(userType)) {
            notificationService.markAsReadForInstitution(notificationId, userId);
        } else {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @RequestParam String userType,
            @RequestParam Long userId) {

        if ("DONOR".equalsIgnoreCase(userType)) {
            notificationService.markAllAsRead(userId);
        } else if ("INSTITUTION".equalsIgnoreCase(userType)) {
            notificationService.markAllAsReadForInstitution(userId);
        } else {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }

    // ============= DONOR SPECIFIC (Backward Compatibility) =============
    @GetMapping("/donor")
    public ResponseEntity<List<Notification>> getDonorNotifications(
            @RequestHeader("X-Donor-Id") Long donorId) {

        List<Notification> notifications = notificationService.getDonorNotifications(donorId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/donor/unread")
    public ResponseEntity<List<Notification>> getDonorUnreadNotifications(
            @RequestHeader("X-Donor-Id") Long donorId) {

        List<Notification> notifications = notificationService.getUnreadNotifications(donorId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/donor/unread-count")
    public ResponseEntity<Map<String, Long>> getDonorUnreadCount(
            @RequestHeader("X-Donor-Id") Long donorId) {

        Long count = notificationService.getUnreadCount(donorId);

        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);

        return ResponseEntity.ok(response);
    }

    // ============= INSTITUTION SPECIFIC =============
    @GetMapping("/institution/{institutionId}")
    public ResponseEntity<List<Notification>> getInstitutionNotifications(
            @PathVariable Long institutionId) {

        List<Notification> notifications = notificationService.getInstitutionNotifications(institutionId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/institution/{institutionId}/unread")
    public ResponseEntity<List<Notification>> getInstitutionUnreadNotifications(
            @PathVariable Long institutionId) {

        List<Notification> notifications = notificationService.getUnreadNotificationsForInstitution(institutionId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/institution/{institutionId}/unread-count")
    public ResponseEntity<Map<String, Long>> getInstitutionUnreadCount(
            @PathVariable Long institutionId) {

        Long count = notificationService.getUnreadCountForInstitution(institutionId);

        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);

        return ResponseEntity.ok(response);
    }
}
