package org.liftakids.repositories;

import org.liftakids.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findBySponsorshipId(Long sponsorshipId);
    // Find payments by sponsorship ID with sorting
    List<Payment> findBySponsorshipIdOrderByPaymentDateDesc(Long sponsorshipId);
    // Find payments by student ID through sponsorship
    @Query("SELECT p FROM Payment p WHERE p.sponsorship.student.studentId = :studentId")
    List<Payment> findByStudentId(@Param("studentId") Long studentId);

    // Find payments by student ID with sorting
    @Query("SELECT p FROM Payment p WHERE p.sponsorship.student.studentId = :studentId ORDER BY p.paymentDate DESC")
    List<Payment> findByStudentIdOrderByPaymentDateDesc(@Param("studentId") Long studentId);
}
