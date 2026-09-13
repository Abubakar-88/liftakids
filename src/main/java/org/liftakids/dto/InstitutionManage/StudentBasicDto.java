package org.liftakids.dto.InstitutionManage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentBasicDto {
    private Long studentId;
    private String studentName;
    private String guardianName;
    private String contactNumber;
    private Boolean isSponsored;
}