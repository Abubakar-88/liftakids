package org.liftakids.service;

public interface SponsorshipExpiryService {
    void expireOldPendingSponsorships();
    void sendPendingReminders();
    void cleanupOldNotifications();
}
