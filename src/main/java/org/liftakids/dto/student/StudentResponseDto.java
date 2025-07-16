package org.liftakids.dto.student;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class StudentResponseDto {
    private Long studentId;
    private String studentName;
    private Date dob;
    private String gender;
    private String address;
    private String contactNumber;
    private String financial_rank;
    private String bio;
    private String photoUrl;
    private boolean sponsored;
    private String guardianName;
    private Long institutionId;
     private BigDecimal requiredMonthlySupport;
}
