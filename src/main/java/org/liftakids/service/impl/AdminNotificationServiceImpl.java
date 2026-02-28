package org.liftakids.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.liftakids.dto.notifications.NotificationAnalyticsDTO;
import org.liftakids.dto.notifications.NotificationFilterDTO;
import org.liftakids.dto.notifications.NotificationResponseDTO;
import org.liftakids.entity.Donor;
import org.liftakids.entity.Institutions;
import org.liftakids.entity.Notification;
import org.liftakids.entity.SystemAdmin;
import org.liftakids.entity.enm.NotificationStatus;
import org.liftakids.entity.enm.NotificationType;
import org.liftakids.entity.enm.UserType;
import org.liftakids.exception.ResourceNotFoundException;
import org.liftakids.repositories.AdminRepository;
import org.liftakids.repositories.DonorRepository;
import org.liftakids.repositories.InstitutionRepository;
import org.liftakids.repositories.NotificationRepository;
import org.liftakids.service.AdminNotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AdminNotificationServiceImpl implements AdminNotificationService {


    private final NotificationRepository notificationRepository;
    private final AdminRepository adminRepository;
    private final DonorRepository donorRepository;
    private final InstitutionRepository institutionRepository;

//    @Override
//    public Page<Notification> getAllNotificationsForAdmin(Pageable pageable) {
//        log.info("Admin fetching all notifications with pagination");
//        return notificationRepository.findAllByOrderByCreatedAtDesc(pageable);
//    }
@Override
@Transactional()
public Page<NotificationResponseDTO> getAllNotificationsForAdmin(Pageable pageable) {
    log.info("Admin fetching all notifications with pagination - page: {}, size: {}",
            pageable.getPageNumber(), pageable.getPageSize());

    // Fetch notifications with pagination
    Page<Notification> notifications = notificationRepository.findAllByOrderByCreatedAtDesc(pageable);

    // Convert to DTO
    return notifications.map(notification -> {
        NotificationResponseDTO dto = new NotificationResponseDTO(notification);

        // If userId exists but userName is null, try to get it from donorId/institutionId
        if (dto.getUserName() == null && dto.getUserId() != null) {
            // You might need additional logic here if you want to fetch the name from userId
            // For now, we'll leave it as is since extractUserName already handles it
        }

        return dto;
    });
}
    @Override
    public Page<Notification> getFilteredNotificationsForAdminView(
            NotificationFilterDTO filter,
            Pageable pageable) {

        log.info("Admin filtering notifications with criteria");

        // Handle null filter
        if (filter == null) {
            return notificationRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        // Enum values directly from filter
        UserType userType = filter.getUserType();
        NotificationType notificationType = filter.getNotificationType();
        NotificationStatus status = filter.getStatus();

        // Build query based on available filters
        if (userType != null && notificationType != null && status != null) {
            return notificationRepository.findByUserTypeAndTypeAndStatusOrderByCreatedAtDesc(
                    userType, notificationType, status, pageable);
        } else if (userType != null && status != null) {
            return notificationRepository.findByUserTypeAndStatusOrderByCreatedAtDesc(
                    userType, status, pageable);
        } else if (userType != null) {
            return notificationRepository.findByUserTypeOrderByCreatedAtDesc(userType, pageable);
        } else if (status != null) {
            return notificationRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else if (notificationType != null) {
            // Temporary fix - use findAll and filter in memory
            List<Notification> filtered = notificationRepository.findAll()
                    .stream()
                    .filter(n -> notificationType.equals(n.getType()))
                    .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                    .collect(Collectors.toList());

            // Manual pagination
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), filtered.size());
            List<Notification> pageContent = filtered.subList(start, end);

            return new PageImpl<>(pageContent, pageable, filtered.size());
        } else if (filter.getStartDate() != null && filter.getEndDate() != null) {
            return notificationRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(
                    filter.getStartDate(), filter.getEndDate(), pageable);
        } else {
            // Return all if no filter
            return notificationRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
    }

    @Override
    public List<Notification> getNotificationsByCriteria(
            UserType userType,
            NotificationType notificationType,
            NotificationStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        log.info("Getting notifications by criteria");

        // Get all notifications first
        List<Notification> allNotifications = notificationRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

        // Filter in memory (not efficient for large datasets, but works)
        List<Notification> filteredNotifications = new ArrayList<>();

        for (Notification notification : allNotifications) {
            boolean matches = true;

            // Check userType
            if (userType != null && !userType.equals(notification.getUserType())) {
                matches = false;
            }

            // Check notificationType
            if (notificationType != null && !notificationType.equals(notification.getType())) {
                matches = false;
            }

            // Check status
            if (status != null && !status.equals(notification.getStatus())) {
                matches = false;
            }

            // Check startDate
            if (startDate != null && notification.getCreatedAt() != null
                    && notification.getCreatedAt().isBefore(startDate)) {
                matches = false;
            }

            // Check endDate
            if (endDate != null && notification.getCreatedAt() != null
                    && notification.getCreatedAt().isAfter(endDate)) {
                matches = false;
            }

            if (matches) {
                filteredNotifications.add(notification);
            }
        }

        return filteredNotifications;
    }

    @Override
    public Map<String, Object> getNotificationStatistics() {
        log.info("Getting notification statistics");

        Map<String, Object> stats = new HashMap<>();

        // Total counts
        long totalNotifications = notificationRepository.count();
        long totalUnread = notificationRepository.countByStatus(NotificationStatus.UNREAD);
        long totalRead = notificationRepository.countByStatus(NotificationStatus.READ);

        stats.put("totalNotifications", totalNotifications);
        stats.put("totalUnread", totalUnread);
        stats.put("totalRead", totalRead);

        // Count by user type - FIXED
        Map<String, Long> countByUserType = new HashMap<>();
        for (UserType userType : UserType.values()) {
            // Assuming you have countByUserType method that returns long
            long count = notificationRepository.countByUserType(userType);
            countByUserType.put(userType.name(), count);
        }
        stats.put("countByUserType", countByUserType);

        // Count by notification type - Optimized
        Map<String, Long> countByNotificationType = notificationRepository
                .findAll()
                .stream()
                .filter(n -> n.getType() != null)
                .collect(Collectors.groupingBy(
                        n -> n.getType().name(),
                        Collectors.counting()
                ));
        stats.put("countByNotificationType", countByNotificationType);

        // Daily counts for last 7 days
        List<Map<String, Object>> dailyCounts = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDateTime dayStart = LocalDateTime.now().minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = dayStart.plusDays(1);
            long dayCount = notificationRepository.countByCreatedAtBetween(dayStart, dayEnd);

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", dayStart.toLocalDate());
            dayData.put("count", dayCount);
            dailyCounts.add(dayData);
        }
        stats.put("dailyCounts", dailyCounts);

        // Read rate
        double readRate = totalNotifications > 0 ?
                (totalRead * 100.0) / totalNotifications : 0;
        stats.put("readRate", String.format("%.2f%%", readRate));

        return stats;
    }

    // ============= NOTIFICATION CREATION METHODS =============

    @Override
    @Transactional
    public Notification createAdminNotificationForSpecificAdmin(
            Long adminId,
            String title,
            String message,
            NotificationType type,
            String actionUrl,
            String relatedEntityType,
            Long relatedEntityId) {

        log.info("Creating admin notification for adminId: {}", adminId);

        SystemAdmin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with ID: " + adminId));

        SystemAdmin createdByAdmin = admin; // Assuming current admin is creating

        Notification notification = new Notification();

        // RECIPIENT INFO
        notification.setUserType(UserType.ADMIN);
        notification.setUserId(adminId);
        notification.setAdmin(admin);

        // NOTIFICATION CONTENT
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);

        // STATUS & TIMESTAMPS
        notification.setStatus(NotificationStatus.UNREAD);
        notification.setCreatedAt(LocalDateTime.now());

        // Use setter method if exists, otherwise comment out
        // notification.setCreatedBy(createdByAdmin.getName());

        // ACTION & NAVIGATION
        notification.setActionUrl(actionUrl);
        notification.setActionText(getAdminActionText(type));

        // RELATED ENTITY
        notification.setRelatedEntityType(relatedEntityType);
        notification.setRelatedEntityId(relatedEntityId);

        // SENDER INFO (system admin who created this)
        notification.setSenderName(createdByAdmin.getName());
        notification.setSenderType("ADMIN");
        // notification.setSenderId(createdByAdmin.getAdminId());

        // PRIORITY & CATEGORY
        notification.setPriority(getAdminPriority(type));
        notification.setCategory(getAdminCategory(type));

        // DELIVERY STATUS
        notification.setInAppSent(true);
        notification.setEmailSent(false);
        notification.setSmsSent(false);
        notification.setPushSent(false);

        // METADATA
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("createdByAdminId", createdByAdmin.getAdminId());
        metadata.put("createdByAdminName", createdByAdmin.getName());
        metadata.put("targetAdminId", adminId);
        metadata.put("targetAdminName", admin.getName());
        metadata.put("relatedEntityId", relatedEntityId);
        metadata.put("relatedEntityType", relatedEntityType);
        metadata.put("timestamp", LocalDateTime.now().toString());

        notification.setMetadata(convertMetadataToJson(metadata));

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Admin notification created successfully: {}", savedNotification.getNotificationId());

        return savedNotification;
    }

    @Override
    @Transactional
    public Notification createInstitutionNotificationForAdmin(
            Long institutionId,
            String title,
            String message,
            NotificationType type,
            String actionUrl,
            String relatedEntityType,
            Long relatedEntityId,
            Long adminId) {

        log.info("Creating institution notification by admin {} for institutionId: {}", adminId, institutionId);

        Institutions institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found with ID: " + institutionId));

        SystemAdmin createdByAdmin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with ID: " + adminId));

        Notification notification = new Notification();

        // RECIPIENT INFO
        notification.setUserType(UserType.INSTITUTION);
        notification.setUserId(institutionId);
        notification.setInstitution(institution);

        // NOTIFICATION CONTENT
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);

        // STATUS & TIMESTAMPS
        notification.setStatus(NotificationStatus.UNREAD);
        notification.setCreatedAt(LocalDateTime.now());

        // ACTION & NAVIGATION
        notification.setActionUrl(actionUrl);
        notification.setActionText(getInstitutionActionText(type));

        // RELATED ENTITY
        notification.setRelatedEntityType(relatedEntityType);
        notification.setRelatedEntityId(relatedEntityId);

        // SENDER INFO
        notification.setSenderName(createdByAdmin.getName());
        notification.setSenderType("ADMIN");
        // notification.setSenderId(createdByAdmin.getAdminId());

        // PRIORITY & CATEGORY
        notification.setPriority(getInstitutionPriority(type));
        notification.setCategory(getInstitutionCategory(type));

        // DELIVERY STATUS
        notification.setInAppSent(true);
        notification.setEmailSent(true);
        notification.setSmsSent(false);
        notification.setPushSent(false);

        // METADATA
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("createdByAdminId", createdByAdmin.getAdminId());
        metadata.put("createdByAdminName", createdByAdmin.getName());
        metadata.put("institutionId", institutionId);
        metadata.put("institutionName", institution.getInstitutionName());
        metadata.put("institutionEmail", institution.getEmail());
        metadata.put("relatedEntityId", relatedEntityId);
        metadata.put("relatedEntityType", relatedEntityType);
        metadata.put("timestamp", LocalDateTime.now().toString());

        notification.setMetadata(convertMetadataToJson(metadata));

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Institution notification created by admin {}: {}", adminId, savedNotification.getNotificationId());

        return savedNotification;
    }

    @Override
    @Transactional
    public Notification createDonorNotificationForAdmin(
            Long donorId,
            String title,
            String message,
            NotificationType type,
            String actionUrl,
            String relatedEntityType,
            Long relatedEntityId,
            Long adminId) {

        log.info("Creating donor notification by admin {} for donorId: {}", adminId, donorId);

        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found with ID: " + donorId));

        SystemAdmin createdByAdmin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with ID: " + adminId));

        Notification notification = new Notification();

        // RECIPIENT INFO
        notification.setUserType(UserType.DONOR);
        notification.setUserId(donorId);
        notification.setDonor(donor);

        // NOTIFICATION CONTENT
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);

        // STATUS & TIMESTAMPS
        notification.setStatus(NotificationStatus.UNREAD);
        notification.setCreatedAt(LocalDateTime.now());

        // ACTION & NAVIGATION
        notification.setActionUrl(actionUrl);
        notification.setActionText(getDonorActionText(type));

        // RELATED ENTITY
        notification.setRelatedEntityType(relatedEntityType);
        notification.setRelatedEntityId(relatedEntityId);

        // SENDER INFO
        notification.setSenderName(createdByAdmin.getName());
        notification.setSenderType("ADMIN");
        // notification.setSenderId(createdByAdmin.getAdminId());

        // PRIORITY & CATEGORY
        notification.setPriority(getDonorPriority(type));
        notification.setCategory(getDonorCategory(type));

        // DELIVERY STATUS
        notification.setInAppSent(true);
        notification.setEmailSent(true);
        notification.setSmsSent(true);
        notification.setPushSent(false);

        // METADATA
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("createdByAdminId", createdByAdmin.getAdminId());
        metadata.put("createdByAdminName", createdByAdmin.getName());
        metadata.put("donorId", donorId);
        metadata.put("donorName", donor.getName());
        metadata.put("donorEmail", donor.getEmail());
        metadata.put("donorPhone", donor.getPhone());
        metadata.put("relatedEntityId", relatedEntityId);
        metadata.put("relatedEntityType", relatedEntityType);
        metadata.put("timestamp", LocalDateTime.now().toString());

        notification.setMetadata(convertMetadataToJson(metadata));

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Donor notification created by admin {}: {}", adminId, savedNotification.getNotificationId());

        return savedNotification;
    }

    // ============= HELPER METHODS =============

    private String getAdminActionText(NotificationType type) {
        if (type == null) return "View Details";

        switch (type) {
            case ADMIN_ALERT:
                return "Take Action";
            case SYSTEM_ANNOUNCEMENT:
                return "View Announcement";
            case SECURITY_ALERT:
                return "Review Security";
            default:
                return "View Details";
        }
    }

    private String getInstitutionActionText(NotificationType type) {
        if (type == null) return "View Details";

        switch (type) {
            case INSTITUTION_APPROVED:
                return "Go to Dashboard";
            case INSTITUTION_REJECTED:
                return "Contact Support";
            case INSTITUTION_SUSPENDED:
                return "Review Account";
            case PAYMENT_RECEIVED:
                return "View Payment";
            default:
                return "View Details";
        }
    }

    private String getDonorActionText(NotificationType type) {
        if (type == null) return "View Details";

        switch (type) {
            case PAYMENT_RECEIVED:
            case PAYMENT_CONFIRMED:
                return "View Payment";
            case SPONSORSHIP_CREATED:
            case SPONSORSHIP_EXPIRED:
                return "View Sponsorship";
            case DONOR_REGISTRATION:
                return "Complete Profile";
            default:
                return "View Details";
        }
    }

    private String getAdminPriority(NotificationType type) {
        if (type == null) return "MEDIUM";

        switch (type) {
            case SECURITY_ALERT:
            case ADMIN_ALERT:
                return "HIGH";
            default:
                return "MEDIUM";
        }
    }

    private String getInstitutionPriority(NotificationType type) {
        if (type == null) return "MEDIUM";

        switch (type) {
            case INSTITUTION_SUSPENDED:
            case INSTITUTION_APPROVED:
                return "HIGH";
            case INSTITUTION_REJECTED:
            case INSTITUTION_PROFILE_UPDATE:
                return "MEDIUM";
            default:
                return "LOW";
        }
    }

    private String getDonorPriority(NotificationType type) {
        if (type == null) return "MEDIUM";

        switch (type) {
            case DONOR_PAYMENT_FAILED:
            case DONOR_SPONSORSHIP_ENDED:
                return "HIGH";
            case DONOR_PAYMENT_DUE:
            case SPONSORSHIP_EXPIRED:
                return "MEDIUM";
            default:
                return "LOW";
        }
    }

    private String getAdminCategory(NotificationType type) {
        if (type == null) return "ADMIN";

        if (type.name().contains("SECURITY")) {
            return "SECURITY";
        } else if (type.name().contains("SYSTEM")) {
            return "SYSTEM";
        } else {
            return "ADMIN";
        }
    }

    private String getInstitutionCategory(NotificationType type) {
        if (type == null) return "GENERAL";

        if (type.name().contains("PAYMENT")) {
            return "FINANCIAL";
        } else if (type.name().contains("INSTITUTION")) {
            return "ACCOUNT";
        } else {
            return "GENERAL";
        }
    }

    private String getDonorCategory(NotificationType type) {
        if (type == null) return "GENERAL";

        if (type.name().contains("PAYMENT")) {
            return "FINANCIAL";
        } else if (type.name().contains("SPONSORSHIP")) {
            return "SPONSORSHIP";
        } else if (type.name().contains("DONOR")) {
            return "ACCOUNT";
        } else {
            return "GENERAL";
        }
    }

    private String convertMetadataToJson(Map<String, Object> metadata) {
        try {
            return new ObjectMapper().writeValueAsString(metadata);
        } catch (Exception e) {
            log.error("Failed to convert metadata to JSON", e);
            return "{}";
        }
    }

    // ============= BULK OPERATIONS =============

    @Override
    @Transactional
    public int bulkMarkAsRead(List<Long> notificationIds, Long adminId) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return 0;
        }

        log.info("Admin {} bulk marking {} notifications as read", adminId, notificationIds.size());

        int updated = 0;
        for (Long notificationId : notificationIds) {
            try {
                Notification notification = notificationRepository.findById(notificationId).orElse(null);
                if (notification != null) {
                    notification.setStatus(NotificationStatus.READ);
                    notification.setReadAt(LocalDateTime.now());
                    notificationRepository.save(notification);
                    updated++;
                }
            } catch (Exception e) {
                log.error("Failed to mark notification {} as read: {}", notificationId, e.getMessage());
            }
        }

        log.info("Successfully marked {} notifications as read", updated);
        return updated;
    }

    @Override
    @Transactional
    public int bulkDeleteNotifications(List<Long> notificationIds, Long adminId) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return 0;
        }

        log.info("Admin {} bulk deleting {} notifications", adminId, notificationIds.size());

        int deleted = 0;
        for (Long notificationId : notificationIds) {
            try {
                if (notificationRepository.existsById(notificationId)) {
                    notificationRepository.deleteById(notificationId);
                    deleted++;
                }
            } catch (Exception e) {
                log.error("Failed to delete notification {}: {}", notificationId, e.getMessage());
            }
        }

        log.info("Successfully deleted {} notifications", deleted);
        return deleted;
    }

    @Override
    public NotificationAnalyticsDTO getNotificationAnalytics(
            LocalDateTime startDate,
            LocalDateTime endDate) {

        log.info("Getting notification analytics from {} to {}", startDate, endDate);

        NotificationAnalyticsDTO analytics = new NotificationAnalyticsDTO();
        analytics.setStartDate(startDate);
        analytics.setEndDate(endDate);

        // Get total counts
        long totalInPeriod = notificationRepository.countByCreatedAtBetween(startDate, endDate);
        analytics.setTotalNotifications(totalInPeriod);

        // Get read rate
        long readInPeriod = notificationRepository.countByStatusAndCreatedAtBetween(
                NotificationStatus.READ, startDate, endDate);
        double readRate = totalInPeriod > 0 ? (readInPeriod * 100.0) / totalInPeriod : 0;
        analytics.setReadRate(readRate);

        // Simplified analytics
        analytics.setEmailSent(0L);
        analytics.setSmsSent(0L);
        analytics.setPushSent(0L);
        analytics.setAvgResponseTimeHours(0.0);
        analytics.setHourlyDistribution(new ArrayList<>());
        analytics.setTopNotificationTypes(new ArrayList<>());

        return analytics;
    }

    @Override
    @Transactional
    public int clearOldNotifications(int days, Long adminId) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);

        log.info("Admin {} clearing notifications older than {} days (before {})",
                adminId, days, cutoffDate);

        // Find old notifications
        List<Notification> oldNotifications = notificationRepository.findByCreatedAtBefore(cutoffDate);

        int deletedCount = 0;
        for (Notification notification : oldNotifications) {
            try {
                notificationRepository.delete(notification);
                deletedCount++;
            } catch (Exception e) {
                log.error("Failed to delete old notification {}: {}",
                        notification.getNotificationId(), e.getMessage());
            }
        }

        log.info("Cleared {} old notifications", deletedCount);

        // Create audit log if admin exists
        SystemAdmin admin = adminRepository.findById(adminId).orElse(null);
        if (admin != null) {
            createAdminNotificationForSpecificAdmin(
                    admin.getAdminId(),
                    "Old Notifications Cleared",
                    String.format("Cleared %d notifications older than %d days", deletedCount, days),
                    NotificationType.ADMIN_ALERT,
                    "/admin/notifications",
                    "SYSTEM",
                    null
            );
        }

        return deletedCount;
    }

    @Override
    @Transactional
    public List<Notification> resendFailedNotifications(List<Long> notificationIds) {
        log.info("Attempting to resend {} failed notifications", notificationIds.size());

        List<Notification> failedNotifications = notificationRepository.findAllById(notificationIds);
        List<Notification> resentNotifications = new ArrayList<>();

        for (Notification notification : failedNotifications) {
            try {
                // Check if notification actually failed
                if (notification.getStatus() == NotificationStatus.FAILED ||
                        !notification.isEmailSent() ||
                        !notification.isSmsSent() ||
                        !notification.isPushSent()) {

                    // Reset sent flags
                    notification.setEmailSent(false);
                    notification.setSmsSent(false);
                    notification.setPushSent(false);
                    notification.setStatus(NotificationStatus.UNREAD);

                    Notification saved = notificationRepository.save(notification);
                    resentNotifications.add(saved);
                    log.info("Reset notification {} for resending", notification.getNotificationId());
                }
            } catch (Exception e) {
                log.error("Failed to reset notification {} for resending: {}",
                        notification.getNotificationId(), e.getMessage(), e);
            }
        }

        log.info("Successfully reset {} notifications for resending", resentNotifications.size());
        return resentNotifications;
    }
    // ============= MISSING METHODS TO ADD =============

    @Override
    @Transactional
    public void sendSystemAnnouncement(
            String title,
            String message,
            String actionUrl,
            Long adminId) {

        SystemAdmin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with ID: " + adminId));

        log.info("Admin {} sending system announcement: {}", adminId, title);

        // Send to all DONORS
        List<Donor> donors = donorRepository.findAll();
        for (Donor donor : donors) {
            createDonorNotificationForAdmin(
                    donor.getDonorId(),
                    title,
                    message,
                    NotificationType.SYSTEM_ANNOUNCEMENT,
                    actionUrl,
                    "SYSTEM_ANNOUNCEMENT",
                    null,
                    adminId
            );
        }

        // Send to all INSTITUTIONS
        List<Institutions> institutions = institutionRepository.findAll();
        for (Institutions institution : institutions) {
            createInstitutionNotificationForAdmin(
                    institution.getInstitutionsId(),
                    title,
                    message,
                    NotificationType.SYSTEM_ANNOUNCEMENT,
                    actionUrl,
                    "SYSTEM_ANNOUNCEMENT",
                    null,
                    adminId
            );
        }

        // Send to all ADMINS
        List<SystemAdmin> admins = adminRepository.findAll();
        for (SystemAdmin targetAdmin : admins) {
            createAdminNotificationForSpecificAdmin(
                    targetAdmin.getAdminId(),
                    title,
                    message,
                    NotificationType.SYSTEM_ANNOUNCEMENT,
                    actionUrl,
                    "SYSTEM_ANNOUNCEMENT",
                    null
            );
        }

        log.info("System announcement sent to {} donors, {} institutions, and {} admins",
                donors.size(), institutions.size(), admins.size());
    }

    @Override
    @Transactional
    public void sendNotificationToUserType(
            UserType userType,
            String title,
            String message,
            String actionUrl,
            Long adminId) {

        SystemAdmin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with ID: " + adminId));

        log.info("Admin {} sending notification to all {} users", adminId, userType);

        switch (userType) {
            case DONOR:
                List<Donor> donors = donorRepository.findAll();
                for (Donor donor : donors) {
                    createDonorNotificationForAdmin(
                            donor.getDonorId(),
                            title,
                            message,
                            NotificationType.SYSTEM_ANNOUNCEMENT,
                            actionUrl,
                            userType.name(),
                            null,
                            adminId
                    );
                }
                log.info("Sent to {} donors", donors.size());
                break;

            case INSTITUTION:
                List<Institutions> institutions = institutionRepository.findAll();
                for (Institutions institution : institutions) {
                    createInstitutionNotificationForAdmin(
                            institution.getInstitutionsId(),
                            title,
                            message,
                            NotificationType.SYSTEM_ANNOUNCEMENT,
                            actionUrl,
                            userType.name(),
                            null,
                            adminId
                    );
                }
                log.info("Sent to {} institutions", institutions.size());
                break;

            case ADMIN:
                List<SystemAdmin> admins = adminRepository.findAll();
                for (SystemAdmin targetAdmin : admins) {
                    createAdminNotificationForSpecificAdmin(
                            targetAdmin.getAdminId(),
                            title,
                            message,
                            NotificationType.SYSTEM_ANNOUNCEMENT,
                            actionUrl,
                            userType.name(),
                            null
                    );
                }
                log.info("Sent to {} admins", admins.size());
                break;
        }
    }

    public Long getAdminUnreadCount(Long adminId) {
        if (adminId != null) {
            return notificationRepository.countByUserTypeAndUserIdAndStatus(
                    UserType.ADMIN, adminId, NotificationStatus.UNREAD);
        } else {
            return notificationRepository.countByUserTypeAndStatus(
                    UserType.ADMIN, NotificationStatus.UNREAD);
        }
    }
}
