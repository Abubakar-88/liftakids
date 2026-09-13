package org.liftakids.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceResponseDto {
    private Long id;
    private String institution;
    private String position;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Boolean isCurrent;
    private String responsibilities;
    private Long teacherId;
    private String teacherName;
    private Integer totalYears; // Calculated field
}
