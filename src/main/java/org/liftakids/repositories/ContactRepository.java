package org.liftakids.repositories;

import org.liftakids.entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ContactRepository extends JpaRepository<ContactMessage, Long> {

    // Find all messages ordered by creation date (newest first)
    List<ContactMessage> findAllByOrderByCreatedAtDesc();

    // Find unresponded messages ordered by creation date
    List<ContactMessage> findByIsRespondedFalseOrderByCreatedAtDesc();

    // Find responded messages ordered by reply date
    List<ContactMessage> findByIsRespondedTrueOrderByRepliedAtDesc();

    // Find messages by email
    List<ContactMessage> findByEmailOrderByCreatedAtDesc(String email);

    // Find recent messages
    List<ContactMessage> findTop5ByOrderByCreatedAtDesc();

    // Count methods
    long countByIsRespondedFalse();
    long countByIsRespondedTrue();

    // Find messages by date range
    List<ContactMessage> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Custom query for message statistics
    @Query("SELECT COUNT(c) FROM ContactMessage c WHERE c.createdAt BETWEEN :start AND :end")
    long countByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(c) FROM ContactMessage c WHERE c.isResponded = false AND c.createdAt BETWEEN :start AND :end")
    long countUnrespondedByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
