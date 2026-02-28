package org.liftakids.repositories;

import org.liftakids.entity.Institutions;
import org.liftakids.entity.SponsorshipStatus;
import org.liftakids.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student,Long> {

    Page<Student> findByInstitution_InstitutionsId(Long institutionId, Pageable pageable);

    List<Student> findByInstitution(Institutions institution);
    @Query("SELECT s FROM Student s WHERE " +
            "(:studentName IS NULL OR LOWER(s.studentName) LIKE LOWER(CONCAT('%', :studentName, '%'))) AND " +
            "(:guardianName IS NULL OR LOWER(s.guardianName) LIKE LOWER(CONCAT('%', :guardianName, '%'))) AND " +
            "(:contactNumber IS NULL OR s.contactNumber LIKE CONCAT('%', :contactNumber, '%'))")
    List<Student> searchStudents(@Param("studentName") String studentName,
                                 @Param("guardianName") String guardianName,
                                 @Param("contactNumber") String contactNumber);

    @Query("SELECT s FROM Student s WHERE s.isSponsored = :status")
    List<Student> findBySponsorshipStatus(@Param("status") boolean status);

    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.currentSponsorships cs LEFT JOIN FETCH cs.donor LEFT JOIN FETCH s.institution")
    List<Student> findAllWithSponsorships();

    @Query(value = "SELECT s FROM Student s LEFT JOIN FETCH s.currentSponsorships cs LEFT JOIN FETCH cs.donor LEFT JOIN FETCH s.institution",
            countQuery = "SELECT COUNT(s) FROM Student s")
    Page<Student> findAllWithSponsorships(Pageable pageable);

    @Query("SELECT s FROM Student s WHERE " +
            "s.institution.institutionsId = :institutionId " +
            "AND (:studentName IS NULL OR LOWER(s.studentName) LIKE LOWER(CONCAT('%', :studentName, '%'))) " +
            "AND (:guardianName IS NULL OR LOWER(s.guardianName) LIKE LOWER(CONCAT('%', :guardianName, '%'))) " +
            "AND (:contactNumber IS NULL OR s.contactNumber LIKE CONCAT('%', :contactNumber, '%'))")
    List<Student> findByInstitutionWithFilters(
            @Param("institutionId") Long institutionId,
            @Param("studentName") String studentName,
            @Param("guardianName") String guardianName,
            @Param("contactNumber") String contactNumber);

    Page<Student> findByInstitution_InstitutionsIdAndIsSponsored(Long institutionId, boolean isSponsored, Pageable pageable);


    @Query(value = "SELECT * FROM student s WHERE s.is_sponsored = false AND s.financial_rank = 'Urgent' AND s.status = 'ACTIVE' ORDER BY s.created_date DESC LIMIT 3", nativeQuery = true)
    List<Student> findTop3UnsponsoredUrgentStudents();


//    @Query("SELECT s FROM Student s WHERE s.sponsored = false AND s.financialRank = 'URGENT' AND s.status = 'ACTIVE' ORDER BY s.createdDate DESC")
//    List<Student> findUnsponsoredUrgentStudents(Pageable pageable);

    // Find students by institution
    List<Student> findByInstitutionInstitutionsId(Long institutionId);

    // Search students by name within institution
    @Query("SELECT s FROM Student s WHERE s.institution.institutionsId = :institutionId " +
            "AND LOWER(s.studentName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Student> findByInstitutionAndNameContaining(
            @Param("institutionId") Long institutionId,
            @Param("searchTerm") String searchTerm);
    List<Student> findByInstitutionInstitutionsIdAndStudentNameContainingIgnoreCase(
            Long institutionId, String studentName);

    // PENDING_PAYMENT status-এর sponsorship যেসব students-এর আছে
    @Query("SELECT DISTINCT s FROM Student s " +
            "JOIN s.currentSponsorships sp " +
            "WHERE sp.status = :status")
    List<Student> findStudentsWithPendingPaymentSponsorships(
            @Param("status") SponsorshipStatus status);

}
