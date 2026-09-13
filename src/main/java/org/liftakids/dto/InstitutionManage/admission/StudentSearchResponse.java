package org.liftakids.dto.InstitutionManage.admission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentSearchResponse {
    private Long studentId;
    private String studentName;
    private String guardianName;
    private String contactNumber;
    private String className;
    private Boolean isSponsored;
    private Boolean isAdmitted;
}
