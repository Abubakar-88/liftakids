package org.liftakids.repositories.instituteManage;


import org.liftakids.entity.InstituteManage.TeacherClassAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherClassAssignmentRepository extends JpaRepository<TeacherClassAssignment, Long> {

    List<TeacherClassAssignment> findByTeacher_Id(Long teacherId);

    List<TeacherClassAssignment> findByClassName(String className);

    List<TeacherClassAssignment> findByTeacher_IdAndIsActiveTrue(Long teacherId);

    Optional<TeacherClassAssignment> findByTeacher_IdAndClassNameAndSubject(
            Long teacherId, String className, String subject);

    @Query("SELECT DISTINCT tca.className FROM TeacherClassAssignment tca WHERE tca.teacher.id = :teacherId")
    List<String> findDistinctClassesByTeacher(@Param("teacherId") Long teacherId);

    @Query("SELECT COUNT(tca) FROM TeacherClassAssignment tca WHERE tca.className = :className AND tca.isActive = true")
    long countTeachersByClass(@Param("className") String className);



}