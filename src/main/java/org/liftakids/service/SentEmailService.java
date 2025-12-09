package org.liftakids.service;

import org.liftakids.entity.SentEmail;

import java.util.List;
import java.util.Optional;

public interface SentEmailService {
    SentEmail saveSentEmail(SentEmail sentEmail);
    List<SentEmail> getAllSentEmails();
    Optional<SentEmail> getSentEmailById(Long id);
    List<SentEmail> getSentEmailsByRecipient(String email);
    List<SentEmail> getRecentSentEmails();
    List<SentEmail> getTodaySentEmails();
    long getTotalSentEmailsCount();
    long getTodaySentEmailsCount();
}
