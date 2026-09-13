package org.liftakids.dto.InstitutionManage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassSubjectAssignmentDto {
    private Long id;
    private Long classId;
    private String className;
    private Long subjectId;
    private String subjectName;
    private String subjectCode;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}