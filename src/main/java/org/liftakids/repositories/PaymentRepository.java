package org.liftakids.repositories;

import org.liftakids.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Page<Payment> findByIdIn(List<Long> ids, Pageable pageable);

    // Using native query for better performance
    @Query(value = "SELECT p.* FROM payments p " +
            "JOIN sponsorships s ON p.sponsorship_id = s.id " +
            "WHERE s.donor_id = :donorId",
            countQuery = "SELECT COUNT(p.*) FROM payments p " +
                    "JOIN sponsorships s ON p.sponsorship_id = s.id " +
                    "WHERE s.donor_id = :donorId",
            nativeQuery = true)
    Page<Payment> findByDonorId(@Param("donorId") Long donorId, Pageable pageable);

    // Non-paginated version
    @Query(value = "SELECT p.* FROM payments p " +
            "JOIN sponsorships s ON p.sponsorship_id = s.id " +
            "WHERE s.donor_id = :donorId " +
            "ORDER BY p.paymentDate DESC",
            nativeQuery = true)
    List<Payment> findByDonorId(@Param("donorId") Long donorId);
}
