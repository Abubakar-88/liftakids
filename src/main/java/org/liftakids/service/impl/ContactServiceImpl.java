package org.liftakids.service.impl;

import jakarta.transaction.Transactional;
import org.liftakids.dto.contact.ContactReplyDTO;
import org.liftakids.dto.contact.ContactRequestDTO;
import org.liftakids.entity.ContactMessage;
import org.liftakids.repositories.ContactRepository;
import org.liftakids.service.ContactService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ContactServiceImpl implements ContactService {
    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ContactMessage saveMessage(ContactRequestDTO contactRequest) {
        try {
            ContactMessage message = modelMapper.map(contactRequest, ContactMessage.class);

            // ✅ Manually set all required fields
            message.setCreatedAt(LocalDateTime.now());
            message.setIsRead(false);
            message.setIsResponded(false);
            message.setAdminReply(null);
            message.setRepliedAt(null);
            message.setRepliedBy(null);

            System.out.println("✅ Saving contact message: " + message);
            return contactRepository.save(message);

        } catch (Exception e) {
            System.err.println("❌ Error saving contact message: " + e.getMessage());
            throw new RuntimeException("Failed to save contact message: " + e.getMessage(), e);
        }
    }
    @Override
    public ContactMessage saveMessageEntity(ContactMessage message) {
        try {
            if (message.getCreatedAt() == null) {
                message.setCreatedAt(LocalDateTime.now());
            }
            // ✅ Ensure Boolean fields are properly initialized
            if (message.getIsRead() == null) {
                message.setIsRead(false);
            }
            if (message.getIsResponded() == null) {
                message.setIsResponded(false);
            }

            return contactRepository.save(message);

        } catch (Exception e) {
            System.err.println("❌ Error saving contact message entity: " + e.getMessage());
            throw new RuntimeException("Failed to save contact message entity: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ContactMessage> getAllMessages() {
        List<ContactMessage> messages = contactRepository.findAllByOrderByCreatedAtDesc();
        // ✅ Ensure no null Boolean values
        messages.forEach(this::initializeBooleanFields);
        return messages;
    }
    @Override
    public void markAsRead(Long id) {
        ContactMessage message = contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setIsRead(true);
        contactRepository.save(message);
    }

    @Override
    public ContactMessage replyToMessage(Long id, ContactReplyDTO replyRequest, String username) {
        ContactMessage message = contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // Save admin reply
        message.setAdminReply(replyRequest.getReplyMessage());
        message.setRepliedAt(LocalDateTime.now());
        message.setRepliedBy(username != null ? username : "Admin");
        message.setIsRead(false);
        message.setIsResponded(false);

        return contactRepository.save(message);
    }
    @Override
    public Optional<ContactMessage> getMessageById(Long id) {
        return contactRepository.findById(id);
    }
    @Override
    public List<ContactMessage> getUnrespondedMessages() {
        List<ContactMessage> messages = contactRepository.findByIsRespondedFalseOrderByCreatedAtDesc();
        messages.forEach(this::initializeBooleanFields);
        return messages;
    }
    @Override
    public List<ContactMessage> getRespondedMessages() {
        List<ContactMessage> messages = contactRepository.findByIsRespondedTrueOrderByRepliedAtDesc();
        messages.forEach(this::initializeBooleanFields);
        return messages;
    }
    @Override
    // Get message reply history
    public List<ContactMessage> getMessageHistory(String email) {
        return contactRepository.findByEmailOrderByCreatedAtDesc(email);
    }
    @Override
    // Get recent messages (for dashboard)
    public List<ContactMessage> getRecentMessages(int limit) {
        return contactRepository.findTop5ByOrderByCreatedAtDesc()
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
    @Override
    public MessageStats getMessageStats() {
        try {
            List<ContactMessage> allMessages = getAllMessages();
            List<ContactMessage> unresponded = getUnrespondedMessages();
            List<ContactMessage> responded = getRespondedMessages();

            MessageStats stats = new MessageStats();
            stats.setTotalMessages(allMessages.size());
            stats.setUnrespondedCount(unresponded.size());
            stats.setRespondedCount(responded.size());
            stats.setResponseRate(allMessages.isEmpty() ? 0 :
                    (double) responded.size() / allMessages.size() * 100);

            // Today's stats
            LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime todayEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

            long todayMessages = contactRepository.countByCreatedAtBetween(todayStart, todayEnd);
            long todayUnresponded = contactRepository.countUnrespondedByCreatedAtBetween(todayStart, todayEnd);

            stats.setTodayMessages((int) todayMessages);
            stats.setTodayUnresponded((int) todayUnresponded);

            return stats;
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate statistics: " + e.getMessage(), e);
        }
    }
    // Helper method to initialize Boolean fields
    private ContactMessage initializeBooleanFields(ContactMessage message) {
        if (message.getIsRead() == null) {
            message.setIsRead(false);
        }
        if (message.getIsResponded() == null) {
            message.setIsResponded(false);
        }
        return message;
    }
    // Get today's messages
    public List<ContactMessage> getTodayMessages() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        return contactRepository.findByCreatedAtBetween(startOfDay, endOfDay);
    }
    // Get unread messages count
    @Override
    public long getUnreadCount() {
        return contactRepository.countByIsRespondedFalse();
    }

    @Override
    public void deleteMessage(Long id) {
        if (!contactRepository.existsById(id)) {
            throw new RuntimeException("Message not found with id: " + id);
        }
        contactRepository.deleteById(id);
    }

    public static class MessageStats {
        private int totalMessages;
        private int unrespondedCount;
        private int respondedCount;
        private double responseRate;
        private int todayMessages;
        private int todayUnresponded;

        // Getters and Setters
        public int getTotalMessages() { return totalMessages; }
        public void setTotalMessages(int totalMessages) { this.totalMessages = totalMessages; }

        public int getUnrespondedCount() { return unrespondedCount; }
        public void setUnrespondedCount(int unrespondedCount) { this.unrespondedCount = unrespondedCount; }

        public int getRespondedCount() { return respondedCount; }
        public void setRespondedCount(int respondedCount) { this.respondedCount = respondedCount; }

        public double getResponseRate() { return responseRate; }
        public void setResponseRate(double responseRate) { this.responseRate = responseRate; }

        public int getTodayMessages() { return todayMessages; }
        public void setTodayMessages(int todayMessages) { this.todayMessages = todayMessages; }

        public int getTodayUnresponded() { return todayUnresponded; }
        public void setTodayUnresponded(int todayUnresponded) { this.todayUnresponded = todayUnresponded; }
    }

}
