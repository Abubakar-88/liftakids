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
public class ExperienceRequestDto {
    private String institution;
    private String position;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Boolean isCurrent;
    private String responsibilities;
}
