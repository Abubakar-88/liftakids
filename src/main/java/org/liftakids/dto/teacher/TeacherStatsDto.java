package org.liftakids.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherStatsDto {
    private Integer totalStudents;
    private Integer totalClasses;
    private Long totalSubjects;
    private Double attendancePercentage;
}