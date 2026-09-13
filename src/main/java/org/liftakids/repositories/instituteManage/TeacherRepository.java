package org.liftakids.repositories.instituteManage;


import org.liftakids.entity.InstituteManage.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByTeacherId(String teacherId);
    Optional<Teacher> findByEmail(String email);
    List<Teacher> findByInstitution_InstitutionsId(Long institutionId);
    Page<Teacher> findByInstitution_InstitutionsId(Long institutionId, Pageable pageable);
    List<Teacher> findByActiveTrueAndInstitution_InstitutionsId(Long institutionId);
    List<Teacher> findByOnLeaveTrueAndInstitution_InstitutionsId(Long institutionId);

    @Query("SELECT t FROM Teacher t WHERE t.institution.institutionsId = :institutionId AND " +
            "(LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "t.teacherId LIKE CONCAT('%', :keyword, '%'))")
    Page<Teacher> searchTeachers(@Param("institutionId") Long institutionId,
                                 @Param("keyword") String keyword,
                                 Pageable pageable);

    @Query("SELECT COUNT(t) FROM Teacher t WHERE t.institution.institutionsId = :institutionId AND t.active = true")
    long countActiveTeachers(@Param("institutionId") Long institutionId);

    @Query("SELECT COUNT(t) FROM Teacher t WHERE t.institution.institutionsId = :institutionId AND t.onLeave = true")
    long countTeachersOnLeave(@Param("institutionId") Long institutionId);

    @Query("SELECT COUNT(t) FROM Teacher t WHERE t.institution.institutionsId = :institutionId")
    long countTotalTeachers(@Param("institutionId") Long institutionId);
}