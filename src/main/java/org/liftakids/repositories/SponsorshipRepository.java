package org.liftakids.repositories;

import org.liftakids.entity.PaymentMethod;
import org.liftakids.entity.Sponsorship;
import org.liftakids.entity.SponsorshipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SponsorshipRepository extends JpaRepository<Sponsorship, Long> {
    List<Sponsorship> findByStatusAndPaidUpToBeforeOrPaidUpToIsNull(
            SponsorshipStatus status,
            LocalDate date
    );
    // Find active sponsorship by donor and student
    @Query("SELECT s FROM Sponsorship s WHERE s.donor.donorId = :donorId " +
            "AND s.student.studentId = :studentId " +
            "AND s.status = :status")
    Optional<Sponsorship> findByDonorIdAndStudentIdAndStatus(
            @Param("donorId") Long donorId,
            @Param("studentId") Long studentId,
            @Param("status") SponsorshipStatus status);

    @Query("SELECT s FROM Sponsorship s WHERE s.student.studentId = :studentId")
    List<Sponsorship> findByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT s FROM Sponsorship s " +
            "JOIN s.student st " +
            "JOIN s.donor d " +
            "WHERE (:sponsorId IS NULL OR s.id = :sponsorId) " +
            "AND (:studentName IS NULL OR LOWER(st.studentName) LIKE LOWER(CONCAT('%', :studentName, '%'))) " +
            "AND (:donorName IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :donorName, '%'))) " +
            "AND (:overdueOnly IS NULL OR " +
            "   (s.status = org.liftakids.entity.SponsorshipStatus.ACTIVE AND " +
            "   (s.paidUpTo IS NULL OR s.paidUpTo < CURRENT_DATE)))")
    Page<Sponsorship> searchSponsorships(
            @Param("sponsorshipId") Long sponsorId,
            @Param("studentName") String studentName,
            @Param("donorName") String donorName,
            @Param("overdueOnly") Boolean overdueOnly,
            Pageable pageable
    );
    @Query("SELECT s FROM Sponsorship s WHERE s.donor.donorId = :donorId")
    List<Sponsorship> findByDonorId(Long donorId);

    @Query("SELECT s FROM Sponsorship s WHERE s.donor.donorId = :donorId AND s.student.studentId = :studentId")
    List<Sponsorship> findByDonorIdAndStudentId(
            @Param("donorId") Long donorId,
            @Param("studentId") Long studentId
    );

    @Query("SELECT s FROM Sponsorship s WHERE " +
            "s.donor.donorId = :donorId AND " +
            "s.student.studentId = :studentId AND " +
            "((s.startDate <= :endDate AND s.endDate >= :startDate))")
    Optional<Sponsorship> findByDonorIdAndStudentIdAndDateRangeOverlap(
            @Param("donorId") Long donorId,
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    @Query("SELECT s FROM Sponsorship s " +
            "JOIN s.donor d " +
            "JOIN s.student st " +
            "JOIN st.institution i " +
            "WHERE (:sponsorId IS NULL OR d.donorId = :sponsorId) " +
            "AND (:studentName IS NULL OR LOWER(st.studentName) LIKE LOWER(CONCAT('%', :studentName, '%'))) " +
            "AND (:donorName IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :donorName, '%'))) " +
            "AND (:institutionName IS NULL OR LOWER(i.institutionName) LIKE LOWER(CONCAT('%', :institutionName, '%'))) " +
            "AND (:status IS NULL OR s.status = :status) " +
            "AND (:paymentMethod IS NULL OR s.paymentMethod = :paymentMethod) " +
            "AND (:overdueOnly IS NULL OR (:overdueOnly = TRUE AND s.paidUpTo < CURRENT_DATE)) " +
            "AND (:startDate IS NULL OR s.startDate >= :startDate) " +
            "AND (:endDate IS NULL OR s.endDate <= :endDate)")
    Page<Sponsorship> searchSponsorships(
            @Param("sponsorId") Long sponsorId,
            @Param("studentName") String studentName,
            @Param("donorName") String donorName,
            @Param("institutionName") String institutionName,
            @Param("status") SponsorshipStatus status,
            @Param("paymentMethod") PaymentMethod paymentMethod,
            @Param("overdueOnly") Boolean overdueOnly,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
    List<Sponsorship> findByStudent_StudentId(Long studentId);
    //List<Sponsorship> findByStudent_StudentIdAndStatus(Long studentId, SponsorshipStatus status);
    // Find sponsorships by donor ID with pagination
    @Query("SELECT s FROM Sponsorship s WHERE s.donor.donorId = :donorId ORDER BY s.startDate DESC")
    Page<Sponsorship> findByDonorDonorId(@Param("donorId") Long donorId, Pageable pageable);
    List<Sponsorship> findByDonorDonorId(Long donorId);


    // Find sponsorships by institution ID and status
    @Query("SELECT s FROM Sponsorship s " +
            "JOIN s.student st " +
            "JOIN st.institution i " +
            "WHERE i.institutionsId = :institutionId " +
            "AND s.status = :status")
    List<Sponsorship> findByStudentInstitutionIdAndStatus(
            @Param("institutionId") Long institutionId,
            @Param("status") SponsorshipStatus status);

    // Specific query for PENDING_PAYMENT status
    @Query("SELECT s FROM Sponsorship s " +
            "JOIN s.student st " +
            "JOIN st.institution i " +
            "WHERE i.institutionsId = :institutionId " +
            "AND s.status = org.liftakids.entity.SponsorshipStatus.PENDING_PAYMENT " +
            "ORDER BY s.sponsorStartDate DESC")
    List<Sponsorship> findPendingPaymentSponsorships(
            @Param("institutionId") Long institutionId);

    @Query("SELECT s FROM Sponsorship s " +
            "JOIN s.student st " +
            "JOIN st.institution i " +
            "WHERE i.institutionsId = :institutionId")
    List<Sponsorship> findByStudentInstitutionId(@Param("institutionId") Long institutionId);

    List<Sponsorship> findByStudentStudentIdAndStatusAndSponsorStartDateAfter(
            Long studentId,
            SponsorshipStatus status,
            LocalDate sponsorStartDate
    );
    long countByStudentStudentIdAndStatusAndSponsorStartDateAfter(
            Long studentId,
            SponsorshipStatus status,
            LocalDate sponsorStartDate
    );

    List<Sponsorship> findByStatusAndSponsorStartDateBefore(
            SponsorshipStatus status,
            LocalDate date);

    @Query("SELECT s FROM Sponsorship s WHERE s.status = :status " +
            "AND s.sponsorStartDate >= :startDate AND s.sponsorStartDate < :endDate")
    List<Sponsorship> findByStatusAndSponsorStartDateBetween(
            @Param("status") SponsorshipStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
