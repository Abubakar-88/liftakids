package org.liftakids.controller;

import lombok.RequiredArgsConstructor;
import org.liftakids.entity.Notification;
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

    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(
            @RequestHeader("X-Donor-Id") Long donorId) {

        List<Notification> notifications = notificationService.getDonorNotifications(donorId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(
            @RequestHeader("X-Donor-Id") Long donorId) {

        List<Notification> notifications = notificationService.getUnreadNotifications(donorId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @RequestHeader("X-Donor-Id") Long donorId) {

        Long count = notificationService.getUnreadCount(donorId);

        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long notificationId,
            @RequestHeader("X-Donor-Id") Long donorId) {

        notificationService.markAsRead(notificationId, donorId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @RequestHeader("X-Donor-Id") Long donorId) {

        notificationService.markAllAsRead(donorId);
        return ResponseEntity.ok().build();
    }
}
