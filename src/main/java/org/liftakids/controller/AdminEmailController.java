package org.liftakids.controller;

import lombok.RequiredArgsConstructor;
import org.liftakids.service.Util.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/email")
public class AdminEmailController {
    @Value("${app.name:Lift A Kids}")
    private String appName;
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<?> sendCustomEmail(@RequestBody Map<String, String> emailRequest) {
        try {
            String toEmail = emailRequest.get("toEmail");
            String subject = emailRequest.get("subject");
            String message = emailRequest.get("message");
            String senderName = emailRequest.get("senderName");

            // ✅ Enhanced validation
            if (toEmail == null || toEmail.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", "Recipient email is required",
                                "field", "toEmail"
                        ));
            }

            // ✅ Email format validation
            if (!isValidEmail(toEmail)) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", "Invalid email format",
                                "field", "toEmail"
                        ));
            }

            if (subject == null || subject.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", "Email subject is required",
                                "field", "subject"
                        ));
            }

            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", "Email message is required",
                                "field", "message"
                        ));
            }

            // ✅ Trim inputs
            toEmail = toEmail.trim();
            subject = subject.trim();
            message = message.trim();
            senderName = senderName != null ? senderName.trim() : null;

            boolean sent = emailService.sendCustomEmail(toEmail, subject, message, senderName);

            if (sent) {
                // ✅ Log successful email sending
                log.info("📧 Admin email sent - To: {}, Subject: {}", toEmail, subject);

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Email sent successfully to " + toEmail,
                        "toEmail", toEmail,
                        "subject", subject,
                        "timestamp", LocalDateTime.now().toString()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of(
                                "success", false,
                                "message", "Failed to send email. Please check your email configuration.",
                                "toEmail", toEmail
                        ));
            }

        } catch (Exception e) {
            log.error("💥 Error in sendCustomEmail API: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Internal server error: " + e.getMessage()
                    ));
        }
    }

    // ✅ Email validation helper method
    private boolean isValidEmail(String email) {
        if (email == null) return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    // Send email to contact message sender
    @PostMapping("/send-to-contact/{contactId}")
    public ResponseEntity<?> sendEmailToContactSender(
            @PathVariable Long contactId,
            @RequestBody Map<String, String> emailRequest) {
        try {
            // First get the contact message to get the email
            // You'll need to inject ContactService here
            // This is a simplified version - implement based on your contact service

            String subject = emailRequest.get("subject");
            String message = emailRequest.get("message");
            String senderName = emailRequest.get("senderName");

            // Validation
            if (subject == null || subject.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Email subject is required"));
            }
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Email message is required"));
            }

            // For now, return a mock response - you'll need to implement the actual logic
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Email functionality will be implemented with contact service integration",
                    "contactId", contactId
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error sending email: " + e.getMessage()));
        }
    }

    @PostMapping("/send-bulk")
    public ResponseEntity<?> sendBulkEmail(@RequestBody Map<String, Object> bulkRequest) {
        try {
            List<String> toEmails = (List<String>) bulkRequest.get("toEmails");
            String subject = (String) bulkRequest.get("subject");
            String message = (String) bulkRequest.get("message");
            String senderName = (String) bulkRequest.get("senderName");

            if (toEmails == null || toEmails.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Recipient emails are required"));
            }

            int successCount = 0;
            int failureCount = 0;
            List<Map<String, String>> results = new ArrayList<>();

            for (String toEmail : toEmails) {
                boolean sent = emailService.sendCustomEmail(toEmail.trim(), subject, message, senderName);
                if (sent) {
                    successCount++;
                    results.add(Map.of("email", toEmail, "status", "success"));
                } else {
                    failureCount++;
                    results.add(Map.of("email", toEmail, "status", "failed"));
                }
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", String.format("Bulk email sent: %d success, %d failed", successCount, failureCount),
                    "results", results,
                    "successCount", successCount,
                    "failureCount", failureCount
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error sending bulk email: " + e.getMessage()));
        }
    }

    // ✅ Email template sending
    @PostMapping("/send-template")
    public ResponseEntity<?> sendTemplateEmail(@RequestBody Map<String, String> templateRequest) {
        try {
            String toEmail = templateRequest.get("toEmail");
            String templateType = templateRequest.get("templateType");
            Map<String, String> templateData = new HashMap<>();

            // Add template-specific data
            templateRequest.forEach((key, value) -> {
                if (!key.equals("toEmail") && !key.equals("templateType")) {
                    templateData.put(key, value);
                }
            });

            // You can create different templates here
            String subject = "";
            String message = "";

            switch (templateType) {
                case "welcome":
                    subject = "Welcome to " + appName;
                    message = String.format("Hello %s,\n\nWelcome to %s! We're excited to have you on board.\n\nBest regards,\n%s Team",
                            templateData.get("name"), appName, appName);
                    break;
                case "notification":
                    subject = templateData.getOrDefault("subject", "Notification from " + appName);
                    message = templateData.getOrDefault("message", "You have a new notification.");
                    break;
                default:
                    return ResponseEntity.badRequest()
                            .body(Map.of("success", false, "message", "Invalid template type"));
            }

            boolean sent = emailService.sendCustomEmail(toEmail, subject, message, appName + " Team");

            if (sent) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Template email sent successfully",
                        "templateType", templateType,
                        "toEmail", toEmail
                ));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("success", false, "message", "Failed to send template email"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error sending template email: " + e.getMessage()));
        }
    }





}
