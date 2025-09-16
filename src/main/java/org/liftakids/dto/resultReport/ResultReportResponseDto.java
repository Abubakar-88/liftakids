package org.liftakids.dto.resultReport;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ResultReportResponseDto {
    private String examName;
    private String term;
    private LocalDate examDate;
    private Long studentId;
    private String studentName;
    private List<SubjectMarkDto> subjectMarks;
}
