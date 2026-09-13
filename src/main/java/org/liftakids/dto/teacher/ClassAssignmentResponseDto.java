package org.liftakids.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassAssignmentResponseDto {
    private Long id;
    private Long teacherId;
    private String teacherName;
    private String className;
    private String section;
    private String subject;
    private Boolean isClassTeacher;
    private String academicYear;
    private Boolean isActive;
    private String assignedAt;
}