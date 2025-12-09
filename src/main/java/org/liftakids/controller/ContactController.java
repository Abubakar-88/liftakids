package org.liftakids.controller;

import jakarta.validation.Valid;
import org.liftakids.dto.contact.ContactReplyDTO;
import org.liftakids.dto.contact.ContactRequestDTO;
import org.liftakids.entity.ContactMessage;
import org.liftakids.service.ContactService;
import org.liftakids.service.Util.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @Autowired
    private EmailService emailService;

    /**
     * Submit contact form
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitContactForm(
            @Valid @RequestBody ContactRequestDTO contactRequest) {

        try {
            System.out.println("📨 Received contact form: " + contactRequest);

            // Save to database
            ContactMessage message = contactService.saveMessage(contactRequest);
            System.out.println("✅ Message saved with ID: " + message.getId());

            // Send email notification to admin
            boolean notificationSent = emailService.sendContactNotification(contactRequest);
            System.out.println("📧 Admin notification sent: " + notificationSent);

            // Send auto-reply to user
            boolean autoReplySent = emailService.sendAutoReply(contactRequest);
            System.out.println("📧 Auto-reply sent: " + autoReplySent);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Thank you for your message. We'll get back to you soon.");
            response.put("messageId", message.getId());
            response.put("notificationSent", notificationSent);
            response.put("autoReplySent", autoReplySent);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error in contact form submission: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to send message. Please try again.",
                            "error", e.getMessage()
                    ));
        }
    }


    // Get all contact messages with pagination and sorting
    @GetMapping("/messages")
    public ResponseEntity<Map<String, Object>> getAllMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "submittedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            List<ContactMessage> messages = contactService.getAllMessages();

            // Simple pagination (for now - you can implement proper pagination later)
            int start = page * size;
            int end = Math.min(start + size, messages.size());
            List<ContactMessage> paginatedMessages = messages.subList(start, end);

            Map<String, Object> response = new HashMap<>();
            response.put("messages", paginatedMessages);
            response.put("currentPage", page);
            response.put("totalItems", messages.size());
            response.put("totalPages", (int) Math.ceil((double) messages.size() / size));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch messages"));
        }
    }

    // Mark as responded (admin)
//    @PutMapping("/messages/{id}/responded")
//    public ResponseEntity<?> markAsResponded(@PathVariable Long id) {
//        contactService.markAsResponded(id);
//        return ResponseEntity.ok().build();
//    }
    /**
     * Admin reply to contact message
     */
    @PostMapping("/messages/{id}/reply")
    public ResponseEntity<?> replyToMessage(
            @PathVariable Long id,
            @Valid @RequestBody ContactReplyDTO replyRequest,
            @RequestHeader(value = "Username", required = false) String username) {

        try {
            ContactMessage message = contactService.replyToMessage(id, replyRequest, username);

            // Send reply email to user
            boolean replySent = emailService.sendReplyToUser(message, replyRequest.getReplyMessage());

            // Send copy to admin if requested
            boolean copySent = false;
            if (replyRequest.isCopyToAdmin()) {
                copySent = emailService.sendReplyCopyToAdmin(message, replyRequest.getReplyMessage(), username);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Reply sent successfully");
            response.put("messageId", message.getId());
            response.put("replySent", replySent);
            response.put("copySent", copySent);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to send reply",
                            "error", e.getMessage()
                    ));
        }
    }

    // Get message with reply history
    @GetMapping("/messages/{id}/withReply")
    public ResponseEntity<ContactMessage> getMessageWithReply(@PathVariable Long id) {
        return contactService.getMessageById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/messages/unread")
    public ResponseEntity<List<ContactMessage>> getUnrespondedMessages() {
        List<ContactMessage> messages = contactService.getUnrespondedMessages();
        return ResponseEntity.ok(messages);
    }
    // Mark as read
    @PutMapping("/messages/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        try {
            contactService.markAsRead(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Message marked as read"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to mark as read: " + e.getMessage()));
        }
    }

    // Mark message as unread
    @PutMapping("/messages/{id}/unread")
    public ResponseEntity<?> markAsUnread(@PathVariable Long id) {
        try {
            ContactMessage message = contactService.getMessageById(id)
                    .orElseThrow(() -> new RuntimeException("Message not found"));
            message.setIsRead(false);
            contactService.saveMessageEntity(message);
            return ResponseEntity.ok(Map.of("success", true, "message", "Marked as unread"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to mark as unread"));
        }
    }
    // Bulk mark as read
    @PostMapping("/messages/bulk-read")
    public ResponseEntity<?> bulkMarkAsRead(@RequestBody List<Long> messageIds) {
        try {
            for (Long id : messageIds) {
                contactService.markAsRead(id);
            }
            return ResponseEntity.ok(Map.of("success", true, "message", "Messages marked as read"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to mark messages as read"));
        }
    }
    // Get responded messages (for admin)
    @GetMapping("/messages/read")
    public ResponseEntity<List<ContactMessage>> getRespondedMessages() {
        List<ContactMessage> messages = contactService.getRespondedMessages();
        return ResponseEntity.ok(messages);
    }

    // Get message history by email (for admin - to see all messages from a user)
    @GetMapping("/messages/history/{email}")
    public ResponseEntity<List<ContactMessage>> getMessageHistory(@PathVariable String email) {
        List<ContactMessage> history = contactService.getMessageHistory(email);
        return ResponseEntity.ok(history);
    }

     // Get statistics
    @GetMapping("/messages/stats")
    public ResponseEntity<?> getMessageStats() {
        try {
            List<ContactMessage> allMessages = contactService.getAllMessages();
            List<ContactMessage> unresponded = contactService.getUnrespondedMessages();
            List<ContactMessage> responded = contactService.getRespondedMessages();
            List<ContactMessage> todayMessages = contactService.getTodayMessages();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalMessages", allMessages.size());
            stats.put("unrespondedCount", unresponded.size());
            stats.put("respondedCount", responded.size());
            stats.put("todayMessages", todayMessages.size());
            stats.put("responseRate", allMessages.isEmpty() ? 0 :
                    (double) responded.size() / allMessages.size() * 100);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get statistics: " + e.getMessage()));
        }
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long id) {
        try {
            contactService.deleteMessage(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Message deleted successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to delete message"
                    ));
        }
    }
// Get message by ID
    @GetMapping("/messages/{id}")
    public ResponseEntity<?> getMessageById(@PathVariable Long id) {
        try {
            ContactMessage message = contactService.getMessageById(id)
                    .orElseThrow(() -> new RuntimeException("Message not found"));
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    /**
     * Test email service with detailed configuration
     */
    @PostMapping("/test-email")
    public ResponseEntity<?> testEmail(@RequestParam String email) {
        try {
            boolean sent = emailService.sendTestEmail(email);

            if (sent) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Test email sent successfully to " + email,
                        "configuration", Map.of(
                                "host", "smtp.titan.email",
                                "port", 465,
                                "username", "contact@liftakids.org",
                                "protocol", "SMTP/SSL"
                        )
                ));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of(
                                "success", false,
                                "message", "Failed to send test email"
                        ));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to send test email: " + e.getMessage(),
                            "configuration", Map.of(
                                    "host", "smtp.titan.email",
                                    "port", 465,
                                    "username", "contact@liftakid.org",
                                    "protocol", "SMTP/SSL"
                            )
                    ));
        }
    }
}
