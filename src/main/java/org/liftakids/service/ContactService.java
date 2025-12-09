package org.liftakids.service;

import org.liftakids.dto.contact.ContactReplyDTO;
import org.liftakids.dto.contact.ContactRequestDTO;
import org.liftakids.entity.ContactMessage;
import org.liftakids.service.impl.ContactServiceImpl;

import java.util.List;
import java.util.Optional;

public interface ContactService {
  // Save new contact message
    ContactMessage saveMessage(ContactRequestDTO contactRequest);
 ContactMessage saveMessageEntity(ContactMessage message);
   public void markAsRead(Long id);
    // Get all messages
    List<ContactMessage> getAllMessages();
 List<ContactMessage> getTodayMessages();
    // Get message by ID
    Optional<ContactMessage> getMessageById(Long id);
    long getUnreadCount();
    // Reply to message
    ContactMessage replyToMessage(Long id, ContactReplyDTO replyRequest, String username);

    // Get unresponded messages
    List<ContactMessage> getUnrespondedMessages();

    // Get responded messages
    List<ContactMessage> getRespondedMessages();

    // Get message history by email
    List<ContactMessage> getMessageHistory(String email);
    // Get message statistics
    ContactServiceImpl.MessageStats getMessageStats();

 // Get recent messages for dashboard
    List<ContactMessage> getRecentMessages(int limit);

    // Delete message
    void deleteMessage(Long id);
}
