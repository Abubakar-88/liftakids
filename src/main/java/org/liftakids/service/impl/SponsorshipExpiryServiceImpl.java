package org.liftakids.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.liftakids.entity.*;
import org.liftakids.entity.enm.NotificationStatus;
import org.liftakids.entity.enm.NotificationType;
import org.liftakids.entity.enm.UserType;
import org.liftakids.repositories.NotificationRepository;
import org.liftakids.repositories.SponsorshipRepository;
import org.liftakids.service.SponsorshipExpiryService;
import org.liftakids.service.Util.EmailService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SponsorshipExpiryServiceImpl implements SponsorshipExpiryService {
    private final SponsorshipRepository sponsorshipRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 2 * * ?") // প্রতিদিন রাত 2টায়
    @Transactional
    @Override
    public void expireOldPendingSponsorships() {
        try {
            log.info("🔄 Starting sponsorship expiry job...");

            LocalDate threeDaysAgo = LocalDate.now().minusDays(3);

            List<Sponsorship> expired = sponsorshipRepository
                    .findByStatusAndSponsorStartDateBefore(
                            SponsorshipStatus.PENDING_PAYMENT,
                            threeDaysAgo
                    );

            log.info("Found {} expired pending sponsorships", expired.size());

            if (!expired.isEmpty()) {
                List<Notification> notifications = new ArrayList<>();

                expired.forEach(sponsorship -> {
                    // 1. Status update করুন
                    sponsorship.setStatus(SponsorshipStatus.EXPIRED);
                    sponsorship.setUpDateAT(LocalDateTime.now());

                    // 2. Notification তৈরি করুন
                    Notification notification = createExpiryNotification(sponsorship);
                    notifications.add(notification);

                    // 3. Email send করুন (যদি email service থাকে)
                    if (emailService != null) {
                        try {
                            sendExpiryEmail(sponsorship);
                        } catch (Exception e) {
                            log.error("Failed to send email for sponsorship {}: {}",
                                    sponsorship.getId(), e.getMessage());
                        }
                    }

                    log.info("Expired sponsorship ID: {} for donor: {}",
                            sponsorship.getId(),
                            sponsorship.getDonor().getName());
                });

                // Save all changes
                sponsorshipRepository.saveAll(expired);
                notificationRepository.saveAll(notifications);

                log.info("✅ Successfully processed {} expired sponsorships with notifications",
                        expired.size());
            } else {
                log.info("No expired sponsorships found");
            }

        } catch (Exception e) {
            log.error("❌ Error in sponsorship expiry job: {}", e.getMessage(), e);
        }
    }

    private Notification createExpiryNotification(Sponsorship sponsorship) {
        Donor donor = sponsorship.getDonor();
        Institutions institution = sponsorship.getStudent().getInstitution();
        Student student = sponsorship.getStudent();

        return Notification.builder()
                // RECIPIENT INFO
                .userType(UserType.DONOR)
                .userId(donor.getDonorId())
                .donor(donor)
                .institution(institution)

                // NOTIFICATION CONTENT
                .title("Sponsorship Expired")
                .message(String.format(
                        "Your pending sponsorship for %s (Student: %s, ID: %d) has expired because no payment was made within 3 days. " +
                                "Monthly amount: ৳%.2f. You can create a new sponsorship if you still wish to support.",
                        institution.getInstitutionName(),
                        student.getStudentName(),
                        student.getStudentId(),
                        sponsorship.getMonthlyAmount()
                ))
                .shortMessage(String.format(
                        "Sponsorship expired for %s. Amount: ৳%.2f",
                        institution.getInstitutionName(),
                        sponsorship.getMonthlyAmount()
                ))
                .type(NotificationType.SPONSORSHIP_EXPIRED)

                // STATUS & METADATA
                .status(NotificationStatus.UNREAD)
                .createdAt(LocalDateTime.now())
                .inAppSent(true)

                // ACTION & NAVIGATION
                .actionUrl(String.format("/donor/sponsorships/%d", sponsorship.getId()))
                .actionText("View Details")
                .icon("warning")

                // RELATED ENTITY
                .relatedEntityType("SPONSORSHIP")
                .relatedEntityId(sponsorship.getId())
                .relatedEntityName(String.format("Sponsorship #%d", sponsorship.getId()))

                // PRIORITY & CATEGORY
                .priority("MEDIUM")
                .category("FINANCIAL")
                .tags("sponsorship,expired,payment")

                // SENDER INFO
                .senderName("System")
                .senderType("SYSTEM")

                // ADDITIONAL DATA
                .metadata(String.format(
                        "{\"sponsorshipId\": %d, \"donorId\": %d, \"institutionId\": %d, \"studentId\": %d, \"amount\": %.2f}",
                        sponsorship.getId(),
                        donor.getDonorId(),
                        institution.getInstitutionsId(),
                        student.getStudentId(),
                        sponsorship.getMonthlyAmount()
                ))
                .emailSent(true).smsSent(true).pushSent(true).inAppSent(true).build();
    }


    private void sendExpiryEmail(Sponsorship sponsorship) {
        Donor donor = sponsorship.getDonor();
        Student student = sponsorship.getStudent();

        String subject = "Your Sponsorship Has Expired - Lift A Kids";
        String message = String.format(
                "Dear %s,\n\n" +
                        "Your pending sponsorship for student %s has expired because no payment was made within 3 days.\n\n" +
                        "Sponsorship Details:\n" +
                        "- Student: %s\n" +
                        "- Student ID: %d\n" +
                        "- Monthly Amount: ৳%.2f\n" +
                        "- Start Date: %s\n" +
                        "- Status: EXPIRED\n\n" +
                        "If you still wish to sponsor this student, please create a new sponsorship request.\n\n" +
                        "Thank you,\nLift A Kids Team",
                donor.getName(),
                student.getStudentName(),
                student.getStudentName(),
                student.getStudentId(),
                sponsorship.getMonthlyAmount(),
                sponsorship.getSponsorStartDate()
        );

        // Simple email sending (আপনার email service অনুযায়ী modify করুন)
        emailService.sendSimpleEmail(donor.getEmail(), subject, message);

        log.info("📧 Expiry email sent to {}", donor.getEmail());
    }

    // Optional: Reminder notification 2nd day-এ
    @Scheduled(cron = "0 0 12 * * ?") // প্রতিদিন দুপুর 12টায়
    @Transactional
    @Override
    public void sendPendingReminders() {
        try {
            LocalDate twoDaysAgo = LocalDate.now().minusDays(2);
            LocalDate threeDaysAgo = LocalDate.now().minusDays(3);

            List<Sponsorship> reminders = sponsorshipRepository
                    .findByStatusAndSponsorStartDateBetween(
                            SponsorshipStatus.PENDING_PAYMENT,
                            threeDaysAgo,
                            twoDaysAgo
                    );

            if (!reminders.isEmpty()) {
                List<Notification> reminderNotifications = new ArrayList<>();

                reminders.forEach(sponsorship -> {
                    Notification reminder = createReminderNotification(sponsorship);
                    reminderNotifications.add(reminder);

                    log.info("Reminder sent for sponsorship ID: {}", sponsorship.getId());
                });

                notificationRepository.saveAll(reminderNotifications);
                log.info("Sent {} reminder notifications", reminders.size());
            }

        } catch (Exception e) {
            log.error("Error in reminder job: {}", e.getMessage(), e);
        }
    }

    private Notification createReminderNotification(Sponsorship sponsorship) {
        Donor donor = sponsorship.getDonor();
        Institutions institution = sponsorship.getStudent().getInstitution();
        Student student = sponsorship.getStudent();

        return Notification.builder()
                // RECIPIENT INFORMATION (MUST SET THESE)
                .userType(UserType.DONOR)
                .userId(donor.getDonorId())
                .donor(donor)
                .institution(institution)

                // NOTIFICATION CONTENT
                .title("Payment Reminder - 1 Day Left")
                .message(String.format(
                        "Reminder: Your sponsorship for %s (Student: %s) will expire in 1 day if payment is not completed. " +
                                "Monthly amount: ৳%.2f. Please complete the payment to activate the sponsorship.",
                        institution.getInstitutionName(),
                        student.getStudentName(),
                        sponsorship.getMonthlyAmount()
                ))
                .shortMessage(String.format(
                        "Payment reminder: 1 day left for sponsorship of %s",
                        institution.getInstitutionName()
                ))
                .type(NotificationType.PAYMENT_REMINDER)

                // STATUS & METADATA
                .status(NotificationStatus.UNREAD)
                .createdAt(LocalDateTime.now())
                .inAppSent(true)

                // ACTION & NAVIGATION
                .actionUrl(String.format("/donor/payment/%d", sponsorship.getId()))
                .actionText("Make Payment")
                .icon("warning")

                // RELATED ENTITY
                .relatedEntityType("SPONSORSHIP")
                .relatedEntityId(sponsorship.getId())
                .relatedEntityName(String.format("Sponsorship #%d", sponsorship.getId()))

                // PRIORITY & CATEGORY
                .priority("HIGH") // Reminder should be HIGH priority
                .category("FINANCIAL")
                .tags("sponsorship,reminder,payment,urgent")

                // SENDER INFORMATION
                .senderName("System")
                .senderType("SYSTEM")

                // ADDITIONAL DATA
                .metadata(String.format(
                        "{\"sponsorshipId\": %d, \"donorId\": %d, \"institutionId\": %d, \"studentId\": %d, \"amount\": %.2f, \"remainingHours\": 24}",
                        sponsorship.getId(),
                        donor.getDonorId(),
                        institution.getInstitutionsId(),
                        student.getStudentId(),
                        sponsorship.getMonthlyAmount()
                ))
                .emailSent(true).smsSent(true).pushSent(true).build();
    }
    // Clean up old notifications (30 দিনের পুরোনো notification delete)
    @Override
    @Scheduled(cron = "0 0 3 * * ?") // প্রতিদিন রাত 3টায়
    @Transactional
    public void cleanupOldNotifications() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            int deletedCount = notificationRepository.deleteOldNotifications(cutoffDate);
            log.info("🧹 Cleaned up {} old notifications", deletedCount);
        } catch (Exception e) {
            log.error("Error cleaning up notifications: {}", e.getMessage(), e);
        }
    }
}
