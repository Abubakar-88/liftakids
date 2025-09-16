package org.liftakids.service;

import org.liftakids.dto.resultReport.ResultReportRequestDto;
import org.liftakids.dto.resultReport.ResultReportResponseDto;

import java.util.List;

public interface ResultReportService {
    ResultReportResponseDto uploadResult(ResultReportRequestDto dto);
    List<ResultReportResponseDto> getResultsByStudentId(Long studentId);
}

