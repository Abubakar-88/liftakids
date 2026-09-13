package org.liftakids.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeEntryDto {
    private Long classId;
    private Long subjectId;
    private String examName;
    private List<StudentGradeDto> grades;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class StudentGradeDto {
    private Long studentId;
    private Double marks;
    private String grade;
    private String remarks;
}
