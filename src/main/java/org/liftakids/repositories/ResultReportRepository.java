package org.liftakids.repositories;

import org.liftakids.entity.ResultReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResultReportRepository extends JpaRepository<ResultReport, Long> {
//    @Query("SELECT r FROM ResultReport r WHERE r.student.studentId = :studentId")
//    List<ResultReport> findByStudentId(@Param("studentId") Long studentId);
     List<ResultReport> findByStudent_StudentId(Long studentId);
}
