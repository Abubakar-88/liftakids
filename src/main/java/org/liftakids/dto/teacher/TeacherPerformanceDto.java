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
public class TeacherPerformanceDto {
    private Long teacherId;
    private String teacherName;
    private Double averageStudentScore;
    private Double passPercentage;
    private Integer totalStudents;
    private List<SubjectPerformanceDto> subjectPerformance;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class SubjectPerformanceDto {
    private String subjectName;
    private Double averageScore;
    private Double passPercentage;
}