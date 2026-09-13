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
public class InstituteSubjectDto {
    private Long id;
    private String subjectName;
    private String subjectCode;
    private String description;
    private Long institutionId;
    private String institutionName;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
