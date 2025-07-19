package org.liftakids.repositories;

import org.liftakids.entity.ResultReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResultReportRepository extends JpaRepository<ResultReport, Long> {
    List<ResultReport> findByStudent_StudentId(Long studentId);
}
