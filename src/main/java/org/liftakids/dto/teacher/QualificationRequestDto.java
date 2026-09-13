package org.liftakids.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualificationRequestDto {
    private String degree;
    private String institution;
    private Integer year;
    private String grade;
    private Boolean isHighest;
    private String description;
}
