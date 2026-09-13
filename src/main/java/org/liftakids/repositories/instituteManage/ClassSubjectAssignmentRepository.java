package org.liftakids.repositories.instituteManage;

import org.liftakids.entity.InstituteManage.ClassSubjectAssignment;
import org.liftakids.entity.InstituteManage.InstituteSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ClassSubjectAssignmentRepository extends JpaRepository<ClassSubjectAssignment, Long> {

    List<ClassSubjectAssignment> findByInstituteClass_Id(Long classId);

    List<ClassSubjectAssignment> findByInstituteClass_IdAndIsActiveTrue(Long classId);

    @Query("SELECT csa.subject FROM ClassSubjectAssignment csa WHERE csa.instituteClass.id = :classId AND csa.isActive = true")
    List<InstituteSubject> findActiveSubjectsByClassId(@Param("classId") Long classId);

    boolean existsByInstituteClass_IdAndSubject_Id(Long classId, Long subjectId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ClassSubjectAssignment csa WHERE csa.instituteClass.id = :classId")
    void deleteByClassId(@Param("classId") Long classId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ClassSubjectAssignment csa WHERE csa.instituteClass.id = :classId AND csa.subject.id = :subjectId")
    void deleteByClassIdAndSubjectId(@Param("classId") Long classId, @Param("subjectId") Long subjectId);
}
