package org.liftakids.controller;

import lombok.RequiredArgsConstructor;
import org.liftakids.entity.SentEmail;
import org.liftakids.service.SentEmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/sent-emails")
public class SentEmailController {

    private final SentEmailService sentEmailService;

    // Get all sent emails
    @GetMapping
    public ResponseEntity<?> getAllSentEmails() {
        try {
            List<SentEmail> sentEmails = sentEmailService.getAllSentEmails();
            return ResponseEntity.ok(sentEmails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch sent emails: " + e.getMessage()));
        }
    }

    // Get sent email by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getSentEmailById(@PathVariable Long id) {
        try {
            SentEmail sentEmail = sentEmailService.getSentEmailById(id)
                    .orElseThrow(() -> new RuntimeException("Sent email not found with id: " + id));
            return ResponseEntity.ok(sentEmail);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch sent email: " + e.getMessage()));
        }
    }

    // Get sent emails by recipient
    @GetMapping("/recipient/{email}")
    public ResponseEntity<?> getSentEmailsByRecipient(@PathVariable String email) {
        try {
            List<SentEmail> sentEmails = sentEmailService.getSentEmailsByRecipient(email);
            return ResponseEntity.ok(sentEmails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch sent emails: " + e.getMessage()));
        }
    }

    // Get recent sent emails
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentSentEmails() {
        try {
            List<SentEmail> recentEmails = sentEmailService.getRecentSentEmails();
            return ResponseEntity.ok(recentEmails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch recent emails: " + e.getMessage()));
        }
    }

    // Get sent email statistics
    @GetMapping("/stats")
    public ResponseEntity<?> getSentEmailStats() {
        try {
            long totalSent = sentEmailService.getTotalSentEmailsCount();
            long todaySent = sentEmailService.getTodaySentEmailsCount();
            List<SentEmail> recentEmails = sentEmailService.getRecentSentEmails();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalSent", totalSent);
            stats.put("todaySent", todaySent);
            stats.put("recentEmails", recentEmails);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch email statistics: " + e.getMessage()));
        }
    }
}
