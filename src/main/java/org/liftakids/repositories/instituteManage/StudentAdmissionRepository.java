package org.liftakids.repositories.instituteManage;


import org.liftakids.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentAdmissionRepository extends JpaRepository<Student, Long> {

    // ========== Basic Queries ==========

    // Find all students by institution (with pagination)
    Page<Student> findByInstitution_InstitutionsId(Long institutionId, Pageable pageable);

    // Find students by class ID
    List<Student> findByInstituteClass_Id(Long classId);

    // Find students by class ID and institution (for security)
    @Query("SELECT s FROM Student s WHERE s.instituteClass.id = :classId AND s.institution.institutionsId = :institutionId")
    List<Student> findByInstituteClass_IdAndInstitution_InstitutionsId(@Param("classId") Long classId,
                                                                       @Param("institutionId") Long institutionId);

    // Find students who are NOT assigned to any class
    List<Student> findByInstituteClassIsNull();

    // Find unassigned students with pagination (by institution)
    @Query("SELECT s FROM Student s WHERE s.institution.institutionsId = :institutionId AND s.instituteClass IS NULL")
    Page<Student> findUnassignedByInstitution(@Param("institutionId") Long institutionId, Pageable pageable);

    // ========== Search Queries ==========

    // Search by name, guardian, or phone (institution + keyword + unassigned filter combined)
    @Query("SELECT s FROM Student s WHERE s.institution.institutionsId = :institutionId " +
            "AND (:keyword IS NULL OR LOWER(s.studentName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(s.guardianName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(s.contactNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:unassignedOnly = false OR s.instituteClass IS NULL)")
    Page<Student> searchStudentsByInstitutionWithFilters(@Param("institutionId") Long institutionId,
                                                         @Param("keyword") String keyword,
                                                         @Param("unassignedOnly") boolean unassignedOnly,
                                                         Pageable pageable);

    // Search only by keyword (institution + keyword) - without unassigned filter
    @Query("SELECT s FROM Student s WHERE s.institution.institutionsId = :institutionId " +
            "AND (LOWER(s.studentName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(s.guardianName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(s.contactNumber) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Student> searchByInstitutionAndKeyword(@Param("institutionId") Long institutionId,
                                                @Param("keyword") String keyword,
                                                Pageable pageable);

    // Search only unassigned (institution + keyword + unassigned)
    @Query("SELECT s FROM Student s WHERE s.institution.institutionsId = :institutionId " +
            "AND (LOWER(s.studentName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(s.guardianName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(s.contactNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND s.instituteClass IS NULL")
    Page<Student> searchUnassignedByInstitutionAndKeyword(@Param("institutionId") Long institutionId,
                                                          @Param("keyword") String keyword,
                                                          Pageable pageable);

    // ========== Count & Check Queries ==========

    // Check if student already has a class
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Student s " +
            "WHERE s.studentId = :studentId AND s.instituteClass IS NOT NULL")
    boolean existsByInstituteClassIsNotNullAndStudentId(@Param("studentId") Long studentId);

    // Count students by class
    long countByInstituteClass_Id(Long classId);

    // Check if student exists by ID (optional)
    boolean existsByStudentId(Long studentId);
}
