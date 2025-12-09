package org.liftakids.service.impl;

import lombok.RequiredArgsConstructor;
import org.liftakids.entity.SentEmail;
import org.liftakids.repositories.SentEmailRepository;
import org.liftakids.service.SentEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SentEmailServiceImpl implements SentEmailService {

    private final SentEmailRepository sentEmailRepository;

    public SentEmail saveSentEmail(SentEmail sentEmail) {
        return sentEmailRepository.save(sentEmail);
    }

    public List<SentEmail> getAllSentEmails() {
        return sentEmailRepository.findAllByOrderBySentAtDesc();
    }

    public Optional<SentEmail> getSentEmailById(Long id) {
        return sentEmailRepository.findById(id);
    }

    public List<SentEmail> getSentEmailsByRecipient(String email) {
        return sentEmailRepository.findByToEmailOrderBySentAtDesc(email);
    }

    public List<SentEmail> getRecentSentEmails() {
        return sentEmailRepository.findTop10ByOrderBySentAtDesc();
    }

    public List<SentEmail> getTodaySentEmails() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        return sentEmailRepository.findBySentAtBetweenOrderBySentAtDesc(startOfDay, endOfDay);
    }

    public long getTotalSentEmailsCount() {
        return sentEmailRepository.count();
    }

    public long getTodaySentEmailsCount() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        return sentEmailRepository.countBySentAtBetween(startOfDay, endOfDay);
    }
}
