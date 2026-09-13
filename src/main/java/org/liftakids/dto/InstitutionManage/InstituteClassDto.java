package org.liftakids.dto.InstitutionManage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstituteClassDto {
    private Long id;
    private String className;
    private String section;
    private Integer classOrder;
    private Long institutionId;
    private String institutionName;
    private Integer studentCount;
    private Boolean isActive;
    private String academicYear;
    private List<StudentBasicDto> students;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


