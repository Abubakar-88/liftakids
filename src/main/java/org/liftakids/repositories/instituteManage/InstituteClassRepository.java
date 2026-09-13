package org.liftakids.repositories.instituteManage;

import org.liftakids.entity.InstituteManage.InstituteClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstituteClassRepository extends JpaRepository<InstituteClass, Long> {

    List<InstituteClass> findByInstitution_InstitutionsIdOrderByClassOrderAsc(Long institutionId);

    List<InstituteClass> findByInstitution_InstitutionsIdAndIsActiveTrueOrderByClassOrderAsc(Long institutionId);

    Optional<InstituteClass> findByInstitution_InstitutionsIdAndClassNameAndSection(
            Long institutionId, String className, String section);

    @Query("SELECT COUNT(s) FROM Student s WHERE s.instituteClass.id = :classId")
    long countStudentsByClass(@Param("classId") Long classId);

    @Query("SELECT ic FROM InstituteClass ic WHERE ic.institution.institutionsId = :institutionId " +
            "AND ic.className = :className")
    List<InstituteClass> findByInstitutionIdAndClassName(
            @Param("institutionId") Long institutionId,
            @Param("className") String className);

//    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
//            "FROM InstituteClass c " +
//            "WHERE c.institution.institutionsId = :institutionId " +
//            "AND LOWER(c.className) = LOWER(:className) " +
//            "AND (c.section IS NULL OR c.section = :section)")
//    boolean existsByInstitutionIdAndClassNameIgnoreCase(@Param("institutionId") Long institutionId,
//                                                        @Param("className") String className,
//                                                        @Param("section") String section);
    boolean existsByInstitution_InstitutionsIdAndClassName(Long institutionId, String className);

    Optional<InstituteClass> findByIdAndInstitution_InstitutionsId(Long id, Long institutionId);
}
