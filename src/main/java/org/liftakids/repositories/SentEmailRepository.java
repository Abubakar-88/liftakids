package org.liftakids.repositories;

import org.liftakids.entity.SentEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SentEmailRepository extends JpaRepository<SentEmail, Long> {

    // Find all sent emails ordered by sent date (newest first)
    List<SentEmail> findAllByOrderBySentAtDesc();

    // Find emails by recipient
    List<SentEmail> findByToEmailOrderBySentAtDesc(String toEmail);

    // Find emails by date range
    List<SentEmail> findBySentAtBetweenOrderBySentAtDesc(LocalDateTime start, LocalDateTime end);

    // Find successful emails
    List<SentEmail> findBySuccessTrueOrderBySentAtDesc();

    // Find failed emails
    List<SentEmail> findBySuccessFalseOrderBySentAtDesc();

    // Count emails by date range
    @Query("SELECT COUNT(se) FROM SentEmail se WHERE se.sentAt BETWEEN :start AND :end")
    long countBySentAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Find recent emails with limit
    List<SentEmail> findTop10ByOrderBySentAtDesc();
}