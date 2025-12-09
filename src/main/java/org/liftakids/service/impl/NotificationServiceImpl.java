package org.liftakids.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.liftakids.entity.Donor;
import org.liftakids.entity.Institutions;
import org.liftakids.entity.Notification;
import org.liftakids.entity.Sponsorship;
import org.liftakids.entity.enm.NotificationStatus;
import org.liftakids.entity.enm.NotificationType;
import org.liftakids.repositories.NotificationRepository;
import org.liftakids.service.NotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;

    @Override
    public List<Notification> getDonorNotifications(Long donorId) {
        return notificationRepository.findByDonorDonorIdOrderByCreatedAtDesc(donorId);
    }
    @Override
    public List<Notification> getUnreadNotifications(Long donorId) {
        return notificationRepository.findByDonorDonorIdAndStatusOrderByCreatedAtDesc(
                donorId, NotificationStatus.UNREAD);
    }
    @Override
    public Long getUnreadCount(Long donorId) {
        return notificationRepository.countByDonorDonorIdAndStatus(
                donorId, NotificationStatus.UNREAD);
    }

    @Transactional
    @Override
    public void markAsRead(Long notificationId, Long donorId) {
        int updated = notificationRepository.markAsRead(
                notificationId,
                donorId,
                NotificationStatus.READ,
                LocalDateTime.now()
        );

        if (updated == 0) {
            log.warn("Notification {} not found or not owned by donor {}",
                    notificationId, donorId);
        }
    }

    @Transactional
    @Override
    public void markAllAsRead(Long donorId) {
        int updated = notificationRepository.markAllAsRead(
                donorId,
                NotificationStatus.READ,
                LocalDateTime.now()
        );

        log.info("Marked {} notifications as read for donor {}", updated, donorId);
    }

    public Notification createNotification(Donor donor, String title, String message,
                                           NotificationType type, String actionUrl,
                                           String relatedEntityType, Long relatedEntityId,
                                           Institutions institution) {

        Notification notification = Notification.builder()
                .donor(donor)
                .institutions(institution)
                .title(title)
                .message(message)
                .type(type)
                .status(NotificationStatus.UNREAD)
                .createdAt(LocalDateTime.now())
                .actionUrl(actionUrl)
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .build();

        return notificationRepository.save(notification);
    }

    // Example: Payment received notification
    @Override
    public void sendPaymentNotification(Donor donor, Sponsorship sponsorship) {
        createNotification(
                donor,
                "Payment Received",
                String.format("Your payment of ৳%.2f for %s has been received successfully.",
                        sponsorship.getMonthlyAmount(),
                        sponsorship.getStudent().getStudentName()),
                NotificationType.PAYMENT_RECEIVED,
                String.format("/donor/sponsorships/%d", sponsorship.getId()),
                "SPONSORSHIP",
                sponsorship.getId(),
                sponsorship.getStudent().getInstitution()
        );

        log.info("Payment notification sent to donor {}", donor.getEmail());
    }
}
